[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$ImageName = "srishtiyadav1904/containerized-mobile-plan-recommendation-platform",

    [Parameter(Mandatory = $false)]
    [string]$ImageTag = "latest",

    [Parameter(Mandatory = $false)]
    [string]$ContainerName = "mpr-platform-container",

    [Parameter(Mandatory = $false)]
    [int]$HostPort = 8085,

    [Parameter(Mandatory = $false)]
    [int]$ContainerPort = 8080,

    [Parameter(Mandatory = $false)]
    [string]$StateFile = "deployment-state.json",

    [Parameter(Mandatory = $false)]
    [int]$TimeoutSeconds = 90
)

$ErrorActionPreference = "Stop"

$dockerBinDir = "C:\Users\Shreyas\AppData\Local\Programs\DockerDesktop\resources\bin"
if (Test-Path $dockerBinDir) {
    if ($env:PATH -notlike "*$dockerBinDir*") {
        $env:PATH = "$dockerBinDir;$env:PATH"
    }
}

$targetFullImage = "$($ImageName):$($ImageTag)"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "MPR Platform - Container Continuous Deployment" -ForegroundColor Cyan
Write-Host "Target Image    : $targetFullImage" -ForegroundColor Cyan
Write-Host "Container Name  : $ContainerName" -ForegroundColor Cyan
Write-Host "Port Mapping    : Host $HostPort -> Container $ContainerPort" -ForegroundColor Cyan
Write-Host "State File Path : $StateFile" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Enforce Port Allocation Safety Policy
$forbiddenPorts = @(8072, 8081, 8082)
if ($forbiddenPorts -contains $HostPort) {
    Write-Error "[CRITICAL PORT VIOLATION] Host port $HostPort is forbidden. Prohibited ports: $($forbiddenPorts -join ', '). Aborting deployment."
    exit 1
}

# 2. Inspect Existing Container and Preserve Known-Good State for Rollback (Task 14)
$previousDeployment = $null
$existingContainers = (& docker ps -a --filter "name=^/${ContainerName}$" --format "{{.ID}}") 2>$null

if ($existingContainers) {
    $existingContainerId = ($existingContainers -split "`r?`n")[0].Trim()
    $existingImage = (& docker inspect --format='{{.Config.Image}}' $ContainerName) 2>$null
    $existingStatus = (& docker inspect --format='{{.State.Status}}' $ContainerName) 2>$null
    $existingHealth = (& docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' $ContainerName) 2>$null
    $existingCreated = (& docker inspect --format='{{.Created}}' $ContainerName) 2>$null

    Write-Host "[CONTAINER AUDIT] Found existing container '$ContainerName' (ID: $existingContainerId)" -ForegroundColor Yellow
    Write-Host "  Image   : $existingImage"
    Write-Host "  Status  : $existingStatus ($existingHealth)"
    Write-Host "  Created : $existingCreated"

    $previousDeployment = @{
        containerId = if ($existingContainerId) { $existingContainerId.Trim() } else { "" }
        image       = if ($existingImage) { $existingImage.Trim() } else { "unknown" }
        status      = if ($existingStatus) { $existingStatus.Trim() } else { "unknown" }
        health      = if ($existingHealth) { $existingHealth.Trim() } else { "unknown" }
        timestamp   = if ($existingCreated) { $existingCreated.Trim() } else { "" }
    }
} else {
    Write-Host "[CONTAINER AUDIT] No existing container named '$ContainerName' found." -ForegroundColor Gray
}

# 3. Safely Stop and Remove Only the Target Project Container
if ($existingContainers) {
    Write-Host "[LIFECYCLE] Stopping existing container '$ContainerName'..." -ForegroundColor Yellow
    & docker stop $ContainerName | Out-Null
    Write-Host "[LIFECYCLE] Removing existing container '$ContainerName'..." -ForegroundColor Yellow
    & docker rm $ContainerName | Out-Null
    Write-Host "[LIFECYCLE] Container '$ContainerName' safely cleared. Unrelated containers remain untouched." -ForegroundColor Green
}

# 4. Verify Host Port Availability
$portInUse = (& netstat -ano | findstr ":$HostPort") 2>$null
if ($portInUse) {
    Write-Host "[WARN] Host port $HostPort still has active bindings after container removal:" -ForegroundColor Yellow
    $portInUse | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
    Start-Sleep -Seconds 2
}

# 5. Launch Fresh Container with Specified Image Tag
Write-Host "[DEPLOYMENT] Starting fresh container '$ContainerName' using image '$targetFullImage'..." -ForegroundColor Cyan

$dockerRunArgs = @("run", "-d", "-p", "${HostPort}:${ContainerPort}", "--name", $ContainerName, "--restart", "unless-stopped", $targetFullImage)
$newContainerId = (& docker @dockerRunArgs)
if ($LASTEXITCODE -ne 0 -or -not $newContainerId) {
    Write-Error "[DEPLOYMENT FAILED] Failed to launch Docker container '$ContainerName'."
    exit 1
}
$newContainerId = ($newContainerId -split "`r?`n")[0].Trim()
Write-Host "[DEPLOYMENT] Container started successfully with ID: $newContainerId" -ForegroundColor Green

# 6. Post-Deployment Automated Health Check Verification
Write-Host "[HEALTH CHECK] Initiating automated health verification on port $HostPort..." -ForegroundColor Cyan
$healthCheckScript = Join-Path $PSScriptRoot "health-check.ps1"
$healthSuccess = $false

if (Test-Path $healthCheckScript) {
    & powershell -ExecutionPolicy Bypass -File $healthCheckScript -Port $HostPort -ContextPath ROOT -TimeoutSeconds $TimeoutSeconds -RetryIntervalSeconds 3
    if ($LASTEXITCODE -eq 0) {
        $healthSuccess = $true
    }
} else {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    while ($stopwatch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
        try {
            $resp = Invoke-RestMethod -Uri "http://localhost:$HostPort/actuator/health" -TimeoutSec 3 -ErrorAction Stop
            if ($resp -and $resp.status -eq "UP") {
                $healthSuccess = $true
                break
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    }
}

# 7. Record Deployment State and Metadata for Rollback (Task 14)
$currentState = [ordered]@{
    projectName         = "containerized-mobile-plan-recommendation-platform"
    containerName       = $ContainerName
    deployedAt          = (Get-Date).ToString("o")
    currentImage        = $targetFullImage
    containerId         = $newContainerId
    hostPort            = $HostPort
    containerPort       = $ContainerPort
    healthEndpoint      = "http://localhost:$HostPort/actuator/health"
    healthStatus        = if ($healthSuccess) { "UP" } else { "DOWN" }
    previousDeployment  = $previousDeployment
}

try {
    $jsonContent = $currentState | ConvertTo-Json -Depth 5
    Set-Content -Path $StateFile -Value $jsonContent -Force
    Write-Host "[METADATA] Deployment state preserved to '$StateFile' for rollback tracking." -ForegroundColor Green
} catch {
    Write-Host "[WARN] Could not write state file '$StateFile': $($_.Exception.Message)" -ForegroundColor Yellow
}

# 8. Evaluate Outcome and Handle Failure
if (-not $healthSuccess) {
    Write-Host ""
    Write-Host "==========================================================" -ForegroundColor Red
    Write-Host "[CRITICAL] Docker continuous deployment FAILED health verification!" -ForegroundColor Red
    Write-Host "Dumping recent container logs for '$ContainerName':" -ForegroundColor Red
    & docker logs --tail 30 $ContainerName
    Write-Host "==========================================================" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "[DEPLOYMENT COMPLETE] Continuous Deployment verified: $ContainerName is UP and HEALTHY." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
exit 0
