param(
    [string]$BranchName = "develop",
    [string]$AppPort = "8084",
    [bool]$DeployWar = $true,
    [bool]$DeployDocker = $true,
    [string]$DockerHostPort = "8085",
    [bool]$PushDockerHub = $false
)

$ErrorActionPreference = "Stop"
$JenkinsUrl = "http://localhost:8080"
$JobName = "Mobile-Plan-Recommendation-CI"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Triggering Jenkins Pipeline: $JobName" -ForegroundColor Cyan
Write-Host "Branch: $BranchName | AppPort: $AppPort | DeployWar: $DeployWar | DeployDocker: $DeployDocker (Port $DockerHostPort)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Obtain Crumb and Web Session
$webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$crumbUrl = "$JenkinsUrl/crumbIssuer/api/json"
$crumbRes = Invoke-RestMethod -Uri $crumbUrl -WebSession $webSession -Method Get
$crumbHeader = $crumbRes.crumbRequestField
$crumbValue = $crumbRes.crumb

Write-Host "[JENKINS] Acquired CSRF Crumb: $crumbHeader = $crumbValue" -ForegroundColor Gray

# 2. Trigger Parameterized Build
$buildUrl = "$JenkinsUrl/job/$JobName/buildWithParameters?BRANCH_NAME=$BranchName&APP_PORT=$AppPort&DEPLOY_WAR=$($DeployWar.ToString().ToLower())&DEPLOY_DOCKER=$($DeployDocker.ToString().ToLower())&DOCKER_HOST_PORT=$DockerHostPort&PUSH_DOCKER_HUB=$($PushDockerHub.ToString().ToLower())"
$headers = @{
    $crumbHeader = $crumbValue
}

$postRes = Invoke-WebRequest -Uri $buildUrl -Method Post -WebSession $webSession -Headers $headers -MaximumRedirection 0 -ErrorAction SilentlyContinue

$queueItemUrl = $postRes.Headers["Location"]
if (-not $queueItemUrl) {
    Write-Host "[WARN] Location header missing. Checking last build number..." -ForegroundColor Yellow
} else {
    Write-Host "[JENKINS] Build enqueued at: $queueItemUrl" -ForegroundColor Gray
}

# 3. Wait for Queue item to be assigned a Build Number
Write-Host "[JENKINS] Waiting for executor assignment..." -ForegroundColor Yellow
$buildNumber = $null
$attempts = 0

while (-not $buildNumber -and $attempts -lt 30) {
    Start-Sleep -Seconds 2
    $attempts++
    if ($queueItemUrl) {
        try {
            $queueInfo = Invoke-RestMethod -Uri "$($queueItemUrl)api/json" -WebSession $webSession -Method Get
            if ($queueInfo.executable -and $queueInfo.executable.number) {
                $buildNumber = $queueInfo.executable.number
                break
            }
        } catch {
            # Queue item may already have transitioned out
        }
    }
    
    # Fallback check job info
    $jobInfo = Invoke-RestMethod -Uri "$JenkinsUrl/job/$JobName/api/json" -WebSession $webSession -Method Get
    if ($jobInfo.nextBuildNumber) {
        $lastBuild = $jobInfo.lastBuild
        if ($lastBuild) {
            $buildNumber = $lastBuild.number
        }
    }
}

if (-not $buildNumber) {
    Write-Error "[ERROR] Could not determine assigned build number."
    exit 1
}

Write-Host "[JENKINS] Executing Build #$buildNumber on branch '$BranchName'..." -ForegroundColor Green
Write-Host "[JENKINS] Live Console URL: $JenkinsUrl/job/$JobName/$buildNumber/console" -ForegroundColor Cyan

# 4. Stream Console Output
$consoleUrl = "$JenkinsUrl/job/$JobName/$buildNumber/logText/progressiveText"
$startOffset = 0
$isBuilding = $true

while ($isBuilding) {
    Start-Sleep -Seconds 3
    try {
        $logRes = Invoke-WebRequest -Uri "$consoleUrl?start=$startOffset" -WebSession $webSession -Method Get
        $newContent = $logRes.Content
        if ($newContent) {
            Write-Host $newContent -NoNewline
            $startOffset = [int64]$logRes.Headers["X-Text-Size"]
        }
        
        $moreData = $logRes.Headers["X-More-Data"]
        if ($moreData -ne "true") {
            $isBuilding = $false
        }
    } catch {
        # Check if build completed
        $buildInfo = Invoke-RestMethod -Uri "$JenkinsUrl/job/$JobName/$buildNumber/api/json" -WebSession $webSession -Method Get
        if (-not $buildInfo.building) {
            $isBuilding = $false
        }
    }
}

# 5. Final Result Summary
$finalBuildInfo = Invoke-RestMethod -Uri "$JenkinsUrl/job/$JobName/$buildNumber/api/json" -WebSession $webSession -Method Get
$result = $finalBuildInfo.result

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "[JENKINS BUILD #$buildNumber FINISHED] Result: $result" -ForegroundColor $(if ($result -eq "SUCCESS") { "Green" } else { "Red" })
Write-Host "Build Details: $JenkinsUrl/job/$JobName/$buildNumber/" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

if ($result -ne "SUCCESS") {
    exit 2
}
