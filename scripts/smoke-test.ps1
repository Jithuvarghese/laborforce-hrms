# Smoke test for HRMS app (PowerShell)
# Usage: Open PowerShell, ensure app is running on http://localhost:8080, then run this script.
$base = 'http://localhost:8080'
function PostJson($url, $body) {
    $json = $body | ConvertTo-Json -Depth 10
    try {
        return Invoke-RestMethod -Uri $url -Method Post -Body $json -ContentType 'application/json'
    } catch {
        Write-Host "POST $url -> ERROR: $($_.Exception.Message)"
        return $null
    }
}

Write-Host "Creating site..."
$site = PostJson "$base/api/v1/hrms/sites" @{ name = "Smoke Site"; address = "Local" }
if (-not $site) { Write-Host "Site creation failed"; exit 1 }
Write-Host "Site id: $($site.id)"

Write-Host "Creating worker..."
$worker = PostJson "$base/api/v1/hrms/workers" @{ name = "Smoke Worker"; phone = [string](Get-Random -Minimum 9000000000 -Maximum 9999999999) }
if (-not $worker) { Write-Host "Worker creation failed"; exit 1 }
Write-Host "Worker id: $($worker.id)"

Write-Host "Clocking in..."
$clockIn = PostJson "$base/api/attendance/clock-in" @{ workerId = $worker.id; siteId = $site.id }
Write-Host "Clock-in response: $($clockIn | ConvertTo-Json)")

Write-Host "Attempt duplicate clock-in (expect 409)..."
try { Invoke-WebRequest -Uri "$base/api/attendance/clock-in" -Method Post -Body (ConvertTo-Json @{ workerId = $worker.id; siteId = $site.id }) -ContentType 'application/json' -ErrorAction Stop } catch { Write-Host "Duplicate clock-in produced expected error: $($_.Exception.Response.StatusCode)" }

Write-Host "Listing active workers..."
$active = Invoke-RestMethod -Uri "$base/api/attendance/active" -Method Get
Write-Host ($active | ConvertTo-Json)

Write-Host "Clocking out..."
$clockOut = PostJson "$base/api/attendance/clock-out" @{ workerId = $worker.id }
Write-Host "Clock-out response: $($clockOut | ConvertTo-Json)")

Write-Host "Fetching attendance log..."
$log = Invoke-RestMethod -Uri "$base/api/attendance/log?workerId=$($worker.id)" -Method Get
Write-Host ($log | ConvertTo-Json)

Write-Host "Attempt settling current month (expect 400)..."
$now = Get-Date -Format yyyy-MM
try { Invoke-RestMethod -Uri "$base/api/overtime/settle/$($worker.id)?month=$now" -Method Post } catch { Write-Host "Settle produced expected error: $($_.Exception.Message)" }

Write-Host "Smoke test complete."