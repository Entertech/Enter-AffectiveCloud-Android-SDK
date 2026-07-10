#!/bin/sh
set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SUBMODULE_PATH="affective_local_sdk/src/main"
SUBMODULE_ROOT="${ROOT}/${SUBMODULE_PATH}"
HOOK_MARKER="codex-submodule-branch-sync"

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

git_common_dir() {
  git -C "$1" rev-parse --path-format=absolute --git-common-dir
}

backup_existing_hook() {
  hook_file="$1"
  if [ -f "${hook_file}" ] && ! grep -q "${HOOK_MARKER}" "${hook_file}"; then
    cp "${hook_file}" "${hook_file}.before-submodule-sync"
  fi
}

chmod +x "${ROOT}/scripts/submodule_branch_sync.py"
chmod +x "${ROOT}/.githooks/parent/post-checkout"
chmod +x "${ROOT}/.githooks/child/post-commit"

if [ ! -d "${SUBMODULE_ROOT}" ]; then
  echo "Submodule path was not found. Run scripts/bootstrap.sh first." >&2
  exit 1
fi

PARENT_GIT_DIR="$(git_common_dir "${ROOT}")"
CHILD_GIT_DIR="$(git_common_dir "${SUBMODULE_ROOT}")"
PARENT_HOOKS_DIR="${PARENT_GIT_DIR}/hooks"
CHILD_HOOKS_DIR="${CHILD_GIT_DIR}/hooks"
SYNC_STATE_DIR="${PARENT_GIT_DIR}/codex-submodule-sync"
SYNC_SCRIPT="${SYNC_STATE_DIR}/submodule_branch_sync.py"
SYNC_LOG="${SYNC_STATE_DIR}/sync.log"

mkdir -p "${PARENT_HOOKS_DIR}" "${CHILD_HOOKS_DIR}" "${SYNC_STATE_DIR}"
cp "${ROOT}/scripts/submodule_branch_sync.py" "${SYNC_SCRIPT}"
chmod +x "${SYNC_SCRIPT}"

PARENT_HOOK="${PARENT_HOOKS_DIR}/post-checkout"
CHILD_HOOK="${CHILD_HOOKS_DIR}/post-commit"

backup_existing_hook "${PARENT_HOOK}"
backup_existing_hook "${CHILD_HOOK}"

cat > "${PARENT_HOOK}" <<HOOK
#!/bin/sh
# ${HOOK_MARKER} parent
set -eu

ROOT="\$(git rev-parse --show-toplevel 2>/dev/null || true)"
if [ -z "\${ROOT}" ]; then
  exit 0
fi

SYNC_SCRIPT="${SYNC_SCRIPT}"
SYNC_LOG="${SYNC_LOG}"
if [ ! -f "\${SYNC_SCRIPT}" ] && [ -f "\${ROOT}/scripts/submodule_branch_sync.py" ]; then
  SYNC_SCRIPT="\${ROOT}/scripts/submodule_branch_sync.py"
fi
if [ ! -f "\${SYNC_SCRIPT}" ]; then
  exit 0
fi

run_python() {
  if command -v python3 >/dev/null 2>&1; then
    python3 "\$@"
  elif command -v python >/dev/null 2>&1; then
    python "\$@"
  elif command -v py >/dev/null 2>&1; then
    py -3 "\$@"
  else
    echo "Python 3 is required for submodule branch sync." >&2
    exit 1
  fi
}

log_sync() {
  mkdir -p "\$(dirname "\${SYNC_LOG}")" 2>/dev/null || true
  printf '%s parent post-checkout %s\n' "\$(date '+%Y-%m-%dT%H:%M:%S%z')" "\$*" >> "\${SYNC_LOG}" 2>/dev/null || true
}

log_sync "start root=\${ROOT} args=\$*"
if SUBMODULE_SYNC_ROOT="\${ROOT}" run_python "\${SYNC_SCRIPT}" sync-checkout --create-missing --stage-parent; then
  log_sync "ok root=\${ROOT}"
else
  status=\$?
  log_sync "failed status=\${status} root=\${ROOT}"
  exit "\${status}"
fi
HOOK

cat > "${CHILD_HOOK}" <<HOOK
#!/bin/sh
# ${HOOK_MARKER} child
set -eu

PARENT_ROOT="${ROOT}"
SYNC_SCRIPT="${SYNC_SCRIPT}"
SYNC_LOG="${SYNC_LOG}"
SUBMODULE_PATH="${SUBMODULE_PATH}"
if [ ! -f "\${SYNC_SCRIPT}" ] && [ -f "\${PARENT_ROOT}/scripts/submodule_branch_sync.py" ]; then
  SYNC_SCRIPT="\${PARENT_ROOT}/scripts/submodule_branch_sync.py"
fi
if [ ! -f "\${SYNC_SCRIPT}" ]; then
  exit 0
fi

run_python() {
  if command -v python3 >/dev/null 2>&1; then
    python3 "\$@"
  elif command -v python >/dev/null 2>&1; then
    python "\$@"
  elif command -v py >/dev/null 2>&1; then
    py -3 "\$@"
  else
    echo "Python 3 is required for submodule branch sync." >&2
    exit 1
  fi
}

log_sync() {
  mkdir -p "\$(dirname "\${SYNC_LOG}")" 2>/dev/null || true
  printf '%s child post-commit %s\n' "\$(date '+%Y-%m-%dT%H:%M:%S%z')" "\$*" >> "\${SYNC_LOG}" 2>/dev/null || true
}

CHILD_ROOT="\$(git rev-parse --show-toplevel)"
log_sync "start child=\${CHILD_ROOT}"
if SUBMODULE_SYNC_ROOT="\${PARENT_ROOT}" run_python "\${SYNC_SCRIPT}" record-child; then
  log_sync "ok child=\${CHILD_ROOT}"
else
  status=\$?
  log_sync "failed status=\${status} child=\${CHILD_ROOT}"
  exit "\${status}"
fi

if [ "\${SUBMODULE_SYNC_AUTO_PARENT_COMMIT:-0}" = "1" ]; then
  if ! git -C "\${PARENT_ROOT}" diff --cached --quiet -- "\${SUBMODULE_PATH}"; then
    git -C "\${PARENT_ROOT}" commit -m "[codex] update algorithm submodule pointer" -- "\${SUBMODULE_PATH}"
  fi
fi
HOOK

chmod +x "${PARENT_HOOK}" "${CHILD_HOOK}"

git -C "${ROOT}" config core.hooksPath "${PARENT_HOOKS_DIR}"
git -C "${SUBMODULE_ROOT}" config core.hooksPath "${CHILD_HOOKS_DIR}"

SUBMODULE_SYNC_ROOT="${ROOT}" run_python "${SYNC_SCRIPT}" sync-checkout --create-missing --stage-parent

echo "Installed stable parent post-checkout hook and child post-commit hook."
