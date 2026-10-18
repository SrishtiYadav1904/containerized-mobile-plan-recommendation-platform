[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [int]$Port = 8084,

    [Parameter(Mandatory = $false)]
    [AllowEmptyString()]
    [AllowNull()]
    [string]$ContextPath = "mpr-platform",

    [Parameter(Mandatory = $false)]
    [int]$TimeoutSeconds = 60,

    [Parameter(Mandatory = $false)]
    [int]$RetryIntervalSeconds = 3
)

$cleanContext = if ($ContextPath -and $ContextPath.Trim() -ne "" -and $ContextPath.Trim() -ne "/" -and $ContextPath.Trim().ToLower() -ne "root" -and $ContextPath.Trim().ToLower() -ne "none") {
    $ContextPath.Trim("/").Trim() + "/"
} else {
    ""
}
$targetUrl = "http://localhost:$Port/$($cleanContext)actuator/health"
Write-Host "=========================================================="
Write-Host "[HEALTH CHECK] Target URL: $targetUrl"
Write-Host "[HEALTH CHECK] Timeout: ${TimeoutSeconds}s, Interval: ${RetryIntervalSeconds}s"
Write-Host "=========================================================="

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$attempt = 1
$lastError = ""
$lastStatus = 0
$lastBody = ""

while ($stopwatch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
    try {
        $result = Invoke-RestMethod -Uri $targetUrl -TimeoutSec 5 -ErrorAction Stop
        if ($result -and $result.status -eq "UP") {
            $lastBody = $result | ConvertTo-Json -Depth 5 -Compress
            Write-Host ""
            Write-Host "[DEPLOYMENT SUCCESS] Application is UP and healthy on port $Port!"
            Write-Host "[HEALTH CHECK RESULT] Status: UP"
            Write-Host "[HEALTH CHECK RESULT] Details: $lastBody"
            Write-Host "=========================================================="
            exit 0
        } else {
            $lastBody = $result | ConvertTo-Json -Depth 5 -Compress
            $elapsedSec = [int]$stopwatch.Elapsed.TotalSeconds
            Write-Host "[Attempt $attempt - Elapsed: ${elapsedSec}s] Application responded but status is not UP ($lastBody). Waiting..."
        }
    } catch {
        $lastError = $_.Exception.Message
        if ($_.Exception.Response) {
            $lastStatus = [int]$_.Exception.Response.StatusCode
        } else {
            $lastStatus = 0
        }
        $elapsedSec = [int]$stopwatch.Elapsed.TotalSeconds
        Write-Host "[Attempt $attempt - Elapsed: ${elapsedSec}s] Waiting for application (HTTP $lastStatus - $lastError)..."
    }

    $attempt++
    Start-Sleep -Seconds $RetryIntervalSeconds
}

Write-Host ""
Write-Host "=========================================================="
Write-Host "[DEPLOYMENT FAILED] Health check timed out after $TimeoutSeconds seconds."
Write-Host "Target URL : $targetUrl"
Write-Host "Last Status: $lastStatus"
Write-Host "Last Error : $lastError"
if ($lastBody) {
    Write-Host "Last Body  : $lastBody"
}
Write-Host "Diagnostic Tips:"
Write-Host " 1. Check Tomcat logs under logs/ and webapps/$ContextPath."
Write-Host " 2. Verify standalone Tomcat is running on port $Port."
Write-Host " 3. Verify application context path and database connectivity."
Write-Host "=========================================================="
exit 1
