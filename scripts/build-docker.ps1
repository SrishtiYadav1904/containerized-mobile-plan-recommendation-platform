[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$ImageName = "srishtiyadav1904/containerized-mobile-plan-recommendation-platform",

    [Parameter(Mandatory = $false)]
    [string]$ImageTag = "latest",

    [Parameter(Mandatory = $false)]
    [string[]]$AdditionalTags = @("latest", "v0.1.0"),

    [Parameter(Mandatory = $false)]
    [string]$Dockerfile = "Dockerfile",

    [Parameter(Mandatory = $false)]
    [switch]$PushToRegistry,

    [Parameter(Mandatory = $false)]
    [string]$RegistryUser = "srishtiyadav1904"
)

$ErrorActionPreference = "Stop"

$dockerBinDir = "C:\Users\Shreyas\AppData\Local\Programs\DockerDesktop\resources\bin"
if (Test-Path $dockerBinDir) {
    if ($env:PATH -notlike "*$dockerBinDir*") {
        $env:PATH = "$dockerBinDir;$env:PATH"
    }
}

$primaryImageTag = "$($ImageName):$($ImageTag)"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "MPR Platform - Docker Image Packaging" -ForegroundColor Cyan
Write-Host "Primary Tag     : $primaryImageTag" -ForegroundColor Cyan
Write-Host "Additional Tags : $($AdditionalTags -join ', ')" -ForegroundColor Cyan
Write-Host "Dockerfile      : $Dockerfile" -ForegroundColor Cyan
Write-Host "Push Requested  : $PushToRegistry" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Verify WAR exists in target/
$warFiles = Get-ChildItem -Path "target" -Filter "*.war" -ErrorAction SilentlyContinue
if (-not $warFiles -or $warFiles.Count -eq 0) {
    Write-Error "[BUILD FAILED] No WAR file found in 'target/'. Package the application first using 'mvn package -DskipTests'."
    exit 1
}
$warPath = $warFiles[0].FullName
$warSizeMB = [math]::Round($warFiles[0].Length / 1MB, 2)
Write-Host "[ARTIFACT DETECTED] $warPath ($warSizeMB MB)" -ForegroundColor Green

# 2. Construct Docker build tag arguments
$tagArgs = @("-t", $primaryImageTag)
$repoParts = $ImageName -split '/'
$shortName = $repoParts[$repoParts.Length - 1]

foreach ($extraTag in $AdditionalTags) {
    if ($extraTag -and $extraTag -ne $ImageTag) {
        $extraFull = "$($ImageName):$($extraTag)"
        $extraShort = "$($shortName):$($extraTag)"
        $tagArgs += @("-t", $extraFull, "-t", $extraShort)
    }
}

# 3. Build Docker Image
Write-Host "[DOCKER BUILD] Invoking docker build..." -ForegroundColor Cyan
$buildArgs = @("build", "-f", $Dockerfile) + $tagArgs + @(".")
Write-Host "Command: docker $($buildArgs -join ' ')" -ForegroundColor Gray

& docker @buildArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "[DOCKER BUILD FAILED] Docker build exited with code $LASTEXITCODE."
    exit 1
}

# 4. Extract and report image details
$imageId = (& docker images --filter "reference=$primaryImageTag" --format "{{.ID}}") 2>$null
if ($imageId) {
    $firstId = ($imageId -split "`r?`n")[0].Trim()
    $imageSize = (& docker images --filter "reference=$primaryImageTag" --format "{{.Size}}") 2>$null
    Write-Host "[BUILD SUCCESS] Docker image built successfully!" -ForegroundColor Green
    Write-Host "  Image ID : $firstId" -ForegroundColor Green
    Write-Host "  Size     : $imageSize" -ForegroundColor Green
    Write-Host "  Primary  : $primaryImageTag" -ForegroundColor Green
}

# 5. Handle Docker Registry Push
if ($PushToRegistry) {
    Write-Host "[REGISTRY PUSH] Pushing image '$primaryImageTag' to Docker Hub..." -ForegroundColor Cyan
    try {
        & docker push $primaryImageTag
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[REGISTRY SUCCESS] Successfully published '$primaryImageTag' to Docker Hub!" -ForegroundColor Green
            foreach ($extraTag in $AdditionalTags) {
                if ($extraTag -and $extraTag -ne $ImageTag) {
                    $extraFull = "$($ImageName):$($extraTag)"
                    & docker push $extraFull
                }
            }
        } else {
            Write-Host "[REGISTRY NOTICE] Docker push exited with code $LASTEXITCODE. Authentication credentials may be required." -ForegroundColor Yellow
            Write-Host "Run 'docker login -u $RegistryUser' to authenticate, then retry pushing." -ForegroundColor Yellow
        }
    } catch {
        Write-Host "[REGISTRY NOTICE] Push failed: $($_.Exception.Message). Continuing with local image workflow." -ForegroundColor Yellow
    }
} else {
    Write-Host "[REGISTRY NOTICE] Remote push not requested. Local image workflow active." -ForegroundColor Gray
}

Write-Host "==========================================================" -ForegroundColor Cyan
exit 0
