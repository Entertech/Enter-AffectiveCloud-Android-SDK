$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

Set-Location $Root
git submodule update --init --recursive

& (Join-Path $Root "scripts/install_submodule_branch_hooks.ps1")

Write-Host "Bootstrap complete."
