<#
.SYNOPSIS
    Ansible Execution Wrapper for Windows 11 (Task 13).

.DESCRIPTION
    Runs Ansible commands via a containerized runner (mpr-ansible-runner) or WSL2
    without pretending Ansible runs natively in PowerShell.

.PARAMETER Action
    The action to execute: SyntaxCheck, InventoryGraph, CheckMode, RunPlaybook, or Custom.

.PARAMETER Inventory
    Path to inventory file relative to ansible/ (default: inventories/production/hosts.yml).

.PARAMETER Playbook
    Path to playbook file relative to ansible/ (default: playbooks/deploy-mpr-platform.yml).

.PARAMETER ExtraArgs
    Additional raw arguments to pass to the ansible command.

.EXAMPLE
    .\scripts\run-ansible.ps1 -Action SyntaxCheck
    .\scripts\run-ansible.ps1 -Action InventoryGraph
    .\scripts\run-ansible.ps1 -Action CheckMode -Inventory "inventories/local/hosts.yml"
#>

[CmdletBinding()]
param(
    [ValidateSet("SyntaxCheck", "InventoryGraph", "InventoryList", "CheckMode", "RunPlaybook", "Custom")]
    [string]$Action = "SyntaxCheck",

    [string]$Inventory = "inventories/production/hosts.yml",
    [string]$Playbook = "playbooks/deploy-mpr-platform.yml",
    [string]$ExtraArgs = ""
)

$ErrorActionPreference = "Stop"

$workspaceRoot = (Get-Item $PSScriptRoot).Parent.FullName
$ansibleDir = Join-Path $workspaceRoot "ansible"

Write-Host "======================================================================" -ForegroundColor Magenta
Write-Host " Ansible Execution Runner (Task 13)" -ForegroundColor Cyan
Write-Host " Host OS:       Windows 11 (Executing via Docker Desktop Runner)" -ForegroundColor Gray
Write-Host " Action:        $Action" -ForegroundColor Yellow
Write-Host " Workspace:     $workspaceRoot" -ForegroundColor Gray
Write-Host " Inventory:     $Inventory" -ForegroundColor Gray
Write-Host " Playbook:      $Playbook" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Magenta

# Check if mpr-ansible-runner image is available
$runnerImage = "mpr-ansible-runner:latest"
$imageCheck = docker images -q $runnerImage
if (-not $imageCheck) {
    Write-Host "[INFO] Building $runnerImage runner container..." -ForegroundColor Yellow
    docker build -f (Join-Path $workspaceRoot "Dockerfile.ansible") -t $runnerImage $workspaceRoot
}

# Construct Ansible command
$ansibleCmd = ""
switch ($Action) {
    "SyntaxCheck" {
        $ansibleCmd = "ansible-playbook -i $Inventory $Playbook --syntax-check $ExtraArgs"
    }
    "InventoryGraph" {
        $ansibleCmd = "ansible-inventory -i $Inventory --graph $ExtraArgs"
    }
    "InventoryList" {
        $ansibleCmd = "ansible-inventory -i $Inventory --list $ExtraArgs"
    }
    "CheckMode" {
        $ansibleCmd = "ansible-playbook -i $Inventory $Playbook --check $ExtraArgs"
    }
    "RunPlaybook" {
        $ansibleCmd = "ansible-playbook -i $Inventory $Playbook $ExtraArgs"
    }
    "Custom" {
        $ansibleCmd = $ExtraArgs
    }
}

Write-Host "[EXEC] Running inside container: $ansibleCmd" -ForegroundColor Green

# Mount workspace and docker socket to allow container operations
# Normalize path for docker -v
$dockerMount = $ansibleDir.Replace("\", "/")
# Set ANSIBLE_CONFIG explicitly and set container-to-host health DNS resolution
$dockerCmd = "docker run --rm -v `"${ansibleDir}:/ansible`" -v //var/run/docker.sock:/var/run/docker.sock -w /ansible -e ANSIBLE_CONFIG=/ansible/ansible.cfg -e MPR_HEALTH_HOST=host.docker.internal $runnerImage sh -c `"$ansibleCmd`""

Write-Host "[DEBUG] $dockerCmd" -ForegroundColor DarkGray
Invoke-Expression $dockerCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[SUCCESS] Ansible $Action completed successfully with exit code 0." -ForegroundColor Green
} else {
    Write-Host "`n[FAILURE] Ansible $Action failed with exit code $LASTEXITCODE." -ForegroundColor Red
    exit $LASTEXITCODE
}
