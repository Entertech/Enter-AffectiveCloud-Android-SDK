$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$SubmodulePath = "affective_local_sdk/src/main"
$SubmoduleRoot = Join-Path $Root $SubmodulePath
$HookMarker = "codex-submodule-branch-sync"

if (-not (Test-Path (Join-Path $Root ".git"))) {
    throw "Repository root was not found: $Root"
}

if (-not (Test-Path $SubmoduleRoot)) {
    throw "Submodule path was not found. Run scripts/bootstrap.ps1 first."
}

function Get-GitCommonDir([string] $Repo) {
    return (git -C $Repo rev-parse --path-format=absolute --git-common-dir).Trim()
}

function Backup-ExistingHook([string] $HookPath) {
    if (Test-Path $HookPath) {
        $Existing = Get-Content -Raw $HookPath
        if (-not $Existing.Contains($HookMarker)) {
            Copy-Item $HookPath "$HookPath.before-submodule-sync" -Force
        }
    }
}

function Invoke-PythonScript([string[]] $Arguments) {
    foreach ($Candidate in @("python3", "python", "py")) {
        if (Get-Command $Candidate -ErrorAction SilentlyContinue) {
            if ($Candidate -eq "py") {
                & $Candidate -3 @Arguments
            } else {
                & $Candidate @Arguments
            }
            if ($LASTEXITCODE -ne 0) {
                throw "Python command failed: $Candidate $($Arguments -join ' ')"
            }
            return
        }
    }
    throw "Python 3 is required for submodule branch sync."
}

$ParentGitDir = Get-GitCommonDir $Root
$ChildGitDir = Get-GitCommonDir $SubmoduleRoot
$ParentHooksDir = Join-Path $ParentGitDir "hooks"
$ChildHooksDir = Join-Path $ChildGitDir "hooks"
$SyncStateDir = Join-Path $ParentGitDir "codex-submodule-sync"
$SyncScript = Join-Path $SyncStateDir "submodule_branch_sync.py"
$SyncLog = Join-Path $SyncStateDir "sync.log"

New-Item -ItemType Directory -Force -Path $ParentHooksDir, $ChildHooksDir, $SyncStateDir | Out-Null
Copy-Item (Join-Path $Root "scripts/submodule_branch_sync.py") $SyncScript -Force

$ParentHook = Join-Path $ParentHooksDir "post-checkout"
$ChildHook = Join-Path $ChildHooksDir "post-commit"

Backup-ExistingHook $ParentHook
Backup-ExistingHook $ChildHook

$RootForHook = ($Root.ProviderPath -replace "\\", "/")
$SyncScriptForHook = ($SyncScript -replace "\\", "/")
$SyncLogForHook = ($SyncLog -replace "\\", "/")

$ParentHookText = @'
#!/bin/sh
# __HOOK_MARKER__ parent
set -eu

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"
if [ -z "${ROOT}" ]; then
  exit 0
fi

SYNC_SCRIPT="__SYNC_SCRIPT__"
SYNC_LOG="__SYNC_LOG__"
if [ ! -f "${SYNC_SCRIPT}" ] && [ -f "${ROOT}/scripts/submodule_branch_sync.py" ]; then
  SYNC_SCRIPT="${ROOT}/scripts/submodule_branch_sync.py"
fi
if [ ! -f "${SYNC_SCRIPT}" ]; then
  exit 0
fi

run_python() {
  if command -v python3 >/dev/null 2>&1; then
    python3 "$@"
  elif command -v python >/dev/null 2>&1; then
    python "$@"
  elif command -v py >/dev/null 2>&1; then
    py -3 "$@"
  else
    echo "Python 3 is required for submodule branch sync." >&2
    exit 1
  fi
}

log_sync() {
  mkdir -p "$(dirname "${SYNC_LOG}")" 2>/dev/null || true
  printf '%s parent post-checkout %s\n' "$(date '+%Y-%m-%dT%H:%M:%S%z')" "$*" >> "${SYNC_LOG}" 2>/dev/null || true
}

log_sync "start root=${ROOT} args=$*"
if SUBMODULE_SYNC_ROOT="${ROOT}" run_python "${SYNC_SCRIPT}" sync-checkout --create-missing --stage-parent; then
  log_sync "ok root=${ROOT}"
else
  status=$?
  log_sync "failed status=${status} root=${ROOT}"
  exit "${status}"
fi
'@.Replace("__HOOK_MARKER__", $HookMarker).Replace("__SYNC_SCRIPT__", $SyncScriptForHook).Replace("__SYNC_LOG__", $SyncLogForHook)

$ChildHookText = @'
#!/bin/sh
# __HOOK_MARKER__ child
set -eu

PARENT_ROOT="__PARENT_ROOT__"
SYNC_SCRIPT="__SYNC_SCRIPT__"
SYNC_LOG="__SYNC_LOG__"
SUBMODULE_PATH="__SUBMODULE_PATH__"
if [ ! -f "${SYNC_SCRIPT}" ] && [ -f "${PARENT_ROOT}/scripts/submodule_branch_sync.py" ]; then
  SYNC_SCRIPT="${PARENT_ROOT}/scripts/submodule_branch_sync.py"
fi
if [ ! -f "${SYNC_SCRIPT}" ]; then
  exit 0
fi

run_python() {
  if command -v python3 >/dev/null 2>&1; then
    python3 "$@"
  elif command -v python >/dev/null 2>&1; then
    python "$@"
  elif command -v py >/dev/null 2>&1; then
    py -3 "$@"
  else
    echo "Python 3 is required for submodule branch sync." >&2
    exit 1
  fi
}

log_sync() {
  mkdir -p "$(dirname "${SYNC_LOG}")" 2>/dev/null || true
  printf '%s child post-commit %s\n' "$(date '+%Y-%m-%dT%H:%M:%S%z')" "$*" >> "${SYNC_LOG}" 2>/dev/null || true
}

CHILD_ROOT="$(git rev-parse --show-toplevel)"
log_sync "start child=${CHILD_ROOT}"
if SUBMODULE_SYNC_ROOT="${PARENT_ROOT}" run_python "${SYNC_SCRIPT}" record-child; then
  log_sync "ok child=${CHILD_ROOT}"
else
  status=$?
  log_sync "failed status=${status} child=${CHILD_ROOT}"
  exit "${status}"
fi

if [ "${SUBMODULE_SYNC_AUTO_PARENT_COMMIT:-0}" = "1" ]; then
  if ! git -C "${PARENT_ROOT}" diff --cached --quiet -- "${SUBMODULE_PATH}"; then
    git -C "${PARENT_ROOT}" commit -m "[codex] update algorithm submodule pointer" -- "${SUBMODULE_PATH}"
  fi
fi
'@.Replace("__HOOK_MARKER__", $HookMarker).Replace("__PARENT_ROOT__", $RootForHook).Replace("__SYNC_SCRIPT__", $SyncScriptForHook).Replace("__SYNC_LOG__", $SyncLogForHook).Replace("__SUBMODULE_PATH__", $SubmodulePath)

Set-Content -Path $ParentHook -Value $ParentHookText -NoNewline
Set-Content -Path $ChildHook -Value $ChildHookText -NoNewline

if (Get-Command chmod -ErrorAction SilentlyContinue) {
    & chmod +x $ParentHook $ChildHook $SyncScript
}

git -C $Root config core.hooksPath $ParentHooksDir
git -C $SubmoduleRoot config core.hooksPath $ChildHooksDir

$PreviousRoot = $env:SUBMODULE_SYNC_ROOT
$env:SUBMODULE_SYNC_ROOT = $Root.ProviderPath
try {
    Invoke-PythonScript @($SyncScript, "sync-checkout", "--create-missing", "--stage-parent")
} finally {
    if ($null -eq $PreviousRoot) {
        Remove-Item Env:\SUBMODULE_SYNC_ROOT -ErrorAction SilentlyContinue
    } else {
        $env:SUBMODULE_SYNC_ROOT = $PreviousRoot
    }
}

Write-Host "Installed stable parent post-checkout hook and child post-commit hook."
