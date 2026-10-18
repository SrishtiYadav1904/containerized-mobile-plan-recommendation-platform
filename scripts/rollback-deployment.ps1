<#
.SYNOPSIS
    Automated Rollback and Recovery Script for Containerized Mobile Plan Recommendation Platform (Task 14).

.DESCRIPTION
    Safely reverts the deployed application container (mpr-platform-container) to the previous
    known-good image tag upon deployment failure or health-check degradation.
    Strictly safeguards unrelated containers (e.g., driving-school-container) and enforces port safety (port 8085).

.PARAMETER TargetRollbackImage
    Optional explicit Docker image tag to roll back to. If omitted, reads previousDeployment from deployment-state.json.

.PARAMETER ContainerName
    Name of the application container to roll back (default: mpr-platform-container).

.PARAMETER HostPort
    The host port to bind (default: 8085). Ports 8072, 8081, 8082 are strictly forbidden.

.PARAMETER StateFilePath
    Path to the deployment state metadata file (default: deployment-state.json).

.PARAMETER HealthTimeoutSeconds
    Timeout in seconds for post-rollback health verification (default: 90).

.EXAMPLE
    .\scripts\rollback-deployment.ps1
    .\scripts\rollback-deployment.ps1 -TargetRollbackImage "srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0"
#>

[CmdletBinding()]
param(
    [string]$TargetRollbackImage = "",
    [string]$ContainerName = "mpr-platform-container",
    [int]$HostPort = 8085,
    [int]$ContainerPort = 8080,
    [string]$StateFilePath = "deployment-state.json",
    [string]$HealthEndpoint = "http://localhost:8085/actuator/health",
    [int]$HealthTimeoutSeconds = 90
)

$ErrorActionPreference = "Stop"

Write-Host "======================================================================" -ForegroundColor Magenta
Write-Host " Automated Rollback & Self-Healing Procedure (Task 14)" -ForegroundColor Cyan
Write-Host " Target Container:   $ContainerName" -ForegroundColor Yellow
Write-Host " Host Port:          $HostPort (Container Port: $ContainerPort)" -ForegroundColor Yellow
Write-Host " State Metadata:     $StateFilePath" -ForegroundColor Gray
Write-Host " Health Probe:       $HealthEndpoint" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Magenta

# 1. Enforce Non-Negotiable Port Safety Policy
$forbiddenPorts = @(8072, 8081, 8082)
if ($forbiddenPorts -contains $HostPort) {
    Write-Error "[CRITICAL SECURITY VIOLATION] Host port $HostPort is in the forbidden ports list ($($forbiddenPorts -join ', ')). Rollback aborted!"
    exit 1
}

# 2. Locate and Resolve Rollback Image
$workspaceRoot = (Get-Item $PSScriptRoot).Parent.FullName
$fullStatePath = Join-Path $workspaceRoot $StateFilePath

$rollbackImage = $TargetRollbackImage
$degradedImage = "unknown"
$degradedContainerId = "unknown"

if ([string]::IsNullOrWhiteSpace($rollbackImage)) {
    if (Test-Path $fullStatePath) {
        try {
            $stateContent = Get-Content $fullStatePath -Raw | ConvertFrom-Json
            if ($stateContent.previousDeployment -and $stateContent.previousDeployment.image) {
                $rollbackImage = $stateContent.previousDeployment.image
                $degradedImage = $stateContent.currentImage
                $degradedContainerId = $stateContent.containerId
                Write-Host "[INFO] Detected previous known-good image from state file: $rollbackImage" -ForegroundColor Green
            }
        } catch {
            Write-Host "[WARN] Failed to parse state file: $_" -ForegroundColor Yellow
        }
    }

    # Fallback to repository baseline tag if state file does not contain previousDeployment
    if ([string]::IsNullOrWhiteSpace($rollbackImage)) {
        $rollbackImage = "srishtiyadav1904/containerized-mobile-plan-recommendation-platform:v0.1.0"
        Write-Host "[WARN] No state file rollback target found. Falling back to release baseline: $rollbackImage" -ForegroundColor Yellow
    }
}

Write-Host "[TARGET] Rollback Image: $rollbackImage" -ForegroundColor Cyan

# 3. Audit Active Containers & Safeguard Independent Containers
Write-Host "`n[AUDIT] Inspecting active Docker containers..." -ForegroundColor Gray
$allContainers = docker ps -a --format "{{.ID}}\t{{.Names}}\t{{.Status}}"
$runningMpr = docker ps -q --filter "name=^/${ContainerName}$"

if ($runningMpr) {
    $degradedContainerId = $runningMpr
    $inspectInfo = docker inspect --format '{{.Config.Image}}' $runningMpr
    if ($inspectInfo) { $degradedImage = $inspectInfo }
    Write-Host "[ACTION] Stopping degraded container $ContainerName ($runningMpr)..." -ForegroundColor Yellow
    docker stop $ContainerName | Out-Null
    docker rm -f $ContainerName | Out-Null
} else {
    # Check if stopped container exists with that name
    $existingMpr = docker ps -aq --filter "name=^/${ContainerName}$"
    if ($existingMpr) {
        Write-Host "[ACTION] Removing stopped container $ContainerName ($existingMpr)..." -ForegroundColor Yellow
        docker rm -f $ContainerName | Out-Null
    }
}

# Verify unrelated containers (e.g., driving-school-container) are intact
$foreignContainers = docker ps --filter "name=driving-school-container" --format "{{.Names}} ({{.Status}})"
if ($foreignContainers) {
    Write-Host "[VERIFY] Independent container preserved: $foreignContainers" -ForegroundColor Green
}

# 4. Launch Restored Container with Rollback Image
Write-Host "`n[LAUNCH] Starting fresh container with restored known-good image..." -ForegroundColor Green
$dockerRunCmd = "docker run -d --name $ContainerName -p ${HostPort}:${ContainerPort} --restart unless-stopped $rollbackImage"
Write-Host "[EXEC] $dockerRunCmd" -ForegroundColor DarkGray
$newContainerId = Invoke-Expression $dockerRunCmd

if (-not $newContainerId -or $LASTEXITCODE -ne 0) {
    Write-Error "[FATAL] Failed to launch rollback container with image $rollbackImage!"
    exit 1
}

$shortContainerId = $newContainerId.Substring(0, [System.Math]::Min(12, $newContainerId.Length))
Write-Host "[SUCCESS] Rollback container started: $shortContainerId" -ForegroundColor Green

# 5. Verify Post-Rollback Application Health
Write-Host "`n[HEALTH] Polling post-rollback health endpoint: $HealthEndpoint" -ForegroundColor Gray
$deadline = (Get-Date).AddSeconds($HealthTimeoutSeconds)
$healthy = $false
$attempt = 0

while ((Get-Date) -lt $deadline) {
    $attempt++
    try {
        $resp = Invoke-RestMethod -Uri $HealthEndpoint -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($resp -and $resp.status -eq "UP") {
            $healthy = $true
            Write-Host "[HEALTH PASSED] Container reached status: UP after attempt $attempt!" -ForegroundColor Green
            break
        }
    } catch {
        # Waiting for application startup
    }
    Start-Sleep -Seconds 3
}

if (-not $healthy) {
    Write-Host "`n[DIAGNOSTICS] Application failed to report healthy status. Last 30 container logs:" -ForegroundColor Red
    docker logs --tail 30 $ContainerName
    Write-Error "[FATAL] Rollback health verification failed after $HealthTimeoutSeconds seconds!"
    exit 1
}

# 6. Update Deployment State Metadata
$newState = [ordered]@{
    projectName = "containerized-mobile-plan-recommendation-platform"
    containerName = $ContainerName
    action = "ROLLBACK"
    rolledBackAt = (Get-Date).ToString("o")
    currentImage = $rollbackImage
    containerId = $newContainerId
    hostPort = $HostPort
    containerPort = $ContainerPort
    healthEndpoint = $HealthEndpoint
    healthStatus = "UP"
    degradedDeployment = [ordered]@{
        containerId = $degradedContainerId
        image = $degradedImage
        reason = "Automated rollback triggered due to health failure or simulated defect"
    }
}

$newStateJson = $newState | ConvertTo-Json -Depth 5
Set-Content -Path $fullStatePath -Value $newStateJson -Encoding UTF8
Write-Host "[METADATA] Updated $StateFilePath with verified rollback record." -ForegroundColor Green

Write-Host "`n======================================================================" -ForegroundColor Magenta
Write-Host " ROLLBACK & RECOVERY COMPLETED SUCCESSFULLY" -ForegroundColor Green
Write-Host " Active Image:      $rollbackImage" -ForegroundColor Cyan
Write-Host " Container ID:      $shortContainerId" -ForegroundColor Cyan
Write-Host " Port Binding:      $HostPort -> $ContainerPort" -ForegroundColor Cyan
Write-Host " Live Health:       UP ($HealthEndpoint)" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Magenta
