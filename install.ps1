# Copies the mod from dist/ into the game's local mods folder on Windows. No JDK needed.
# If scripts are blocked: powershell -ExecutionPolicy Bypass -File .\install.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$modsDir = if ($env:SOS_MODS_DIR) { $env:SOS_MODS_DIR } else { Join-Path $env:APPDATA "songsofsyx\mods" }
$src = Join-Path $PSScriptRoot "dist\ProperScroll"
if (-not (Test-Path $src)) {
    Write-Error "dist/ProperScroll is missing, run build.sh to recreate it"
}
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null
$dst = Join-Path $modsDir "ProperScroll"
if (Test-Path $dst) {
    Remove-Item -Recurse -Force $dst
}
Copy-Item -Recurse $src $dst
Write-Host "Installed to $dst. Enable 'Proper Scroll' in the game launcher."
