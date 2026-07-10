$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$SubmodulePath = "affective_local_sdk/src/main"
$SubmoduleRoot = Join-Path $Root $SubmodulePath
$ChildHooksPath = Join-Path $Root ".githooks/child"

if (-not (Test-Path (Join-Path $Root ".git"))) {
    throw "Repository root was not found: $Root"
}

if (-not (Test-Path $SubmoduleRoot)) {
    throw "Submodule path was not found. Run scripts/bootstrap.ps1 first."
}

git -C $Root config core.hooksPath ".githooks/parent"
git -C $SubmoduleRoot config core.hooksPath $ChildHooksPath

Write-Host "Installed parent post-checkout hook and child post-commit hook."
