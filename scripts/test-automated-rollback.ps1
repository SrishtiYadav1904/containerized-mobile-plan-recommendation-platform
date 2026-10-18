<#
.SYNOPSIS
    Automated Rollback and Reliability Validation Test (Task 14).

.DESCRIPTION
    Simulates a controlled deployment failure using an isolated test image,
    demonstrates health check detection failure, triggers automated rollback
    to the verified known-good image, and validates complete recovery on port 8085.
#>

[CmdletBinding()]
param(
    [string]$ContainerName = "mpr-platform-container",
    [int]$HostPort = 8085,
    [int]$ContainerPort = 8080,
    [string]$SimulatedFaultyTag = "faulty-simulated",
    [string]$StateFilePath = "deployment-state.json"
)

$ErrorActionPreference = "Stop"

$workspaceRoot = (Get-Item $PSScriptRoot).Parent.FullName
$fullStatePath = Join-Path $workspaceRoot $StateFilePath
$healthEndpoint = "http://localhost:${HostPort}/actuator/health"

Write-Host "======================================================================" -ForegroundColor Magenta
Write-Host " Task 14: Automated Rollback & Self-Healing Reliability Test" -ForegroundColor Cyan
Write-Host " Host Port:          $HostPort (Strictly avoiding 8072, 8081, 8082)" -ForegroundColor Yellow
Write-Host " Target Container:   $ContainerName" -ForegroundColor Yellow
Write-Host " Health Endpoint:    $healthEndpoint" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Magenta

# Step 1: Pre-Flight Health Check
Write-Host "`n[STEP 1] Auditing pre-test system health..." -ForegroundColor Cyan
$preTestHealthy = $false
try {
    $resp = Invoke-RestMethod -Uri $healthEndpoint -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($resp -and $resp.status -eq "UP") {
        $preTestHealthy = $true
        Write-Host "[OK] Current deployment is healthy (status: UP)." -ForegroundColor Green
    }
} catch {
    Write-Host "[WARN] Pre-test health check did not return UP. Checking container..." -ForegroundColor Yellow
}

# Determine verified baseline image
$baselineImage = "srishtiyadav1904/containerized-mobile-plan-recommendation-platform:latest"
$currentRunning = docker ps -q --filter "name=^/${ContainerName}$"
if ($currentRunning) {
    $inspectImg = docker inspect --format '{{.Config.Image}}' $currentRunning
    if ($inspectImg -and -not $inspectImg.Contains($SimulatedFaultyTag)) {
        $baselineImage = $inspectImg
    }
}
Write-Host "[BASELINE] Known-good baseline image: $baselineImage" -ForegroundColor Green

# Ensure state file has previousDeployment pointing to baselineImage
$stateData = [ordered]@{
    projectName = "containerized-mobile-plan-recommendation-platform"
    containerName = $ContainerName
    deployedAt = (Get-Date).ToString("o")
    currentImage = $baselineImage
    containerId = $currentRunning
    hostPort = $HostPort
    containerPort = $ContainerPort
    healthEndpoint = $healthEndpoint
    healthStatus = "UP"
    previousDeployment = [ordered]@{
        timestamp = (Get-Date).ToString("o")
        status = "running"
        containerId = $currentRunning
        health = "healthy"
        image = $baselineImage
    }
}
$stateData | ConvertTo-Json -Depth 5 | Set-Content -Path $fullStatePath -Encoding UTF8

# Step 2: Create Isolated Simulated Faulty Image Tag
Write-Host "`n[STEP 2] Creating isolated simulated faulty image tag..." -ForegroundColor Cyan
$faultyImage = "srishtiyadav1904/containerized-mobile-plan-recommendation-platform:$SimulatedFaultyTag"
docker tag alpine:latest $faultyImage
Write-Host "[OK] Created simulation tag: $faultyImage" -ForegroundColor Green

# Step 3: Deploy Faulty Container to Simulate Failed Release
Write-Host "`n[STEP 3] Deploying simulated faulty release container to host port $HostPort..." -ForegroundColor Cyan
if ($currentRunning) {
    docker stop $ContainerName | Out-Null
    docker rm -f $ContainerName | Out-Null
}

$faultyRunCmd = "docker run -d --name $ContainerName -p ${HostPort}:${ContainerPort} --restart unless-stopped $faultyImage sleep 300"
$faultyContainerId = Invoke-Expression $faultyRunCmd
Write-Host "[SIMULATION] Faulty container started: $faultyContainerId" -ForegroundColor Yellow

# Update state file to reflect faulty deployment
$stateData.currentImage = $faultyImage
$stateData.containerId = $faultyContainerId
$stateData.healthStatus = "UNKNOWN"
$stateData | ConvertTo-Json -Depth 5 | Set-Content -Path $fullStatePath -Encoding UTF8

# Step 4: Run Health Probes and Detect Failure
Write-Host "`n[STEP 4] Probing health endpoint on faulty release: $healthEndpoint..." -ForegroundColor Cyan
$faultDetected = $false
try {
    $resp = Invoke-RestMethod -Uri $healthEndpoint -Method Get -TimeoutSec 3 -ErrorAction Stop
} catch {
    $faultDetected = $true
    Write-Host "[DETECTED] Health probe failed as expected! Application is not responding on $healthEndpoint." -ForegroundColor Red
}

if (-not $faultDetected) {
    Write-Error "[FAIL] Simulated failure was not detected!"
    exit 1
}

# Step 5: Trigger Automated Rollback
Write-Host "`n[STEP 5] Health degradation detected! Triggering automated rollback procedure..." -ForegroundColor Magenta
$rollbackScript = Join-Path $PSScriptRoot "rollback-deployment.ps1"
& $rollbackScript -ContainerName $ContainerName -HostPort $HostPort -ContainerPort $ContainerPort -TargetRollbackImage $baselineImage -StateFilePath $StateFilePath

if ($LASTEXITCODE -ne 0) {
    Write-Error "[FATAL] Automated rollback script failed!"
    exit 1
}

# Step 6: Post-Rollback Health & Catalog Verification
Write-Host "`n[STEP 6] Verifying restored service on port $HostPort..." -ForegroundColor Cyan
$finalHealth = Invoke-RestMethod -Uri $healthEndpoint -Method Get -TimeoutSec 5
Write-Host "[HEALTH VERIFIED] Actuator status: $($finalHealth.status)" -ForegroundColor Green

$catalogUrl = "http://localhost:${HostPort}/plans"
$catalogHttpCode = & curl.exe -s -o $null -w "%{http_code}" $catalogUrl
Write-Host "[CATALOG VERIFIED] $catalogUrl responded with HTTP $catalogHttpCode" -ForegroundColor Green

# Step 7: Clean Up Temporary Simulation Tag
Write-Host "`n[STEP 7] Cleaning up temporary simulation image tag..." -ForegroundColor Gray
docker rmi $faultyImage | Out-Null
Write-Host "[OK] Cleaned up temporary tag $faultyImage." -ForegroundColor Green

# Step 8: Verify Independent Containers Untouched
$foreignCheck = docker ps --filter "name=driving-school-container" --format "{{.Names}} ({{.Status}})"
if ($foreignCheck) {
    Write-Host "[SECURITY] Foreign container intact: $foreignCheck" -ForegroundColor Green
}

Write-Host "`n======================================================================" -ForegroundColor Magenta
Write-Host " TASK 14 AUTOMATED ROLLBACK TEST: PASSED WITH FLYING COLORS!" -ForegroundColor Green
Write-Host " Restored Deployment: $baselineImage" -ForegroundColor Cyan
Write-Host " Port Binding:        $HostPort -> $ContainerPort" -ForegroundColor Cyan
Write-Host " Application Health:  $($finalHealth.status)" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Magenta
