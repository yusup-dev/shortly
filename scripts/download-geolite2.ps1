# Download GeoLite2-Country.mmdb from MaxMind
# Reads credentials from .env at repo root, or from environment variables.
# Usage: .\scripts\download-geolite2.ps1

$ErrorActionPreference = "Stop"

$rootDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$envFile = Join-Path $rootDir ".env"

function Load-DotEnv {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return }
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $eq = $line.IndexOf("=")
        if ($eq -lt 1) { return }
        $key = $line.Substring(0, $eq).Trim()
        $val = $line.Substring($eq + 1).Trim()
        if ($key -and -not (Get-Item -Path "env:$key" -ErrorAction SilentlyContinue)) {
            Set-Item -Path "env:$key" -Value $val
        }
    }
}

Load-DotEnv -Path $envFile

$accountId = $env:MAXMIND_ACCOUNT_ID
$licenseKey = $env:MAXMIND_LICENSE_KEY

if (-not $accountId -or -not $licenseKey) {
    Write-Error "Missing MAXMIND_ACCOUNT_ID or MAXMIND_LICENSE_KEY. Set them in .env or environment variables."
}

$dataDir = Join-Path $rootDir "data"
$tarFile = Join-Path $dataDir "GeoLite2-Country.tar.gz"
$mmdbFile = Join-Path $dataDir "GeoLite2-Country.mmdb"

New-Item -ItemType Directory -Force -Path $dataDir | Out-Null

$url = "https://download.maxmind.com/geoip/databases/GeoLite2-Country/download?suffix=tar.gz"
$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${accountId}:${licenseKey}"))
$headers = @{ Authorization = "Basic $cred" }

Write-Host "Downloading GeoLite2-Country..."
Invoke-WebRequest -Uri $url -Headers $headers -OutFile $tarFile -MaximumRedirection 5

Write-Host "Extracting..."
tar -xzf $tarFile -C $dataDir

$extracted = Get-ChildItem -Path $dataDir -Recurse -Filter "GeoLite2-Country.mmdb" | Select-Object -First 1
if (-not $extracted) {
    Write-Error "GeoLite2-Country.mmdb not found in archive."
}

Move-Item -Force $extracted.FullName $mmdbFile
Get-ChildItem -Path $dataDir -Directory -Filter "GeoLite2-Country_*" | Remove-Item -Recurse -Force
Remove-Item -Force $tarFile -ErrorAction SilentlyContinue

$normalizedPath = $mmdbFile -replace '\\', '/'
Write-Host "Done: $mmdbFile"
Write-Host ""
Write-Host "GEOIP_DATABASE_PATH in .env should be:"
Write-Host "GEOIP_DATABASE_PATH=$normalizedPath"
