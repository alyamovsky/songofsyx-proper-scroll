# Copies the built mod into the game's local mods folder on Windows.
# Build first: ./build.sh in Git Bash, or copy build/mod from another machine.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$modsDir = if ($env:SOS_MODS_DIR) { $env:SOS_MODS_DIR } else { Join-Path $env:APPDATA "songsofsyx\mods" }
$src = Join-Path $PSScriptRoot "build\mod\ProperScroll"
if (-not (Test-Path $src)) {
    Write-Error "Nothing built yet: build/mod/ProperScroll is missing"
}
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null
$dst = Join-Path $modsDir "ProperScroll"
if (Test-Path $dst) {
    Remove-Item -Recurse -Force $dst
}
Copy-Item -Recurse $src $dst
Write-Host "Installed to $dst. Enable 'Proper Scroll' in the game launcher."
