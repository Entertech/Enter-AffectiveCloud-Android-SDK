#!/bin/sh
set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SUBMODULE_PATH="affective_local_sdk/src/main"

chmod +x "${ROOT}/scripts/submodule_branch_sync.py"
chmod +x "${ROOT}/.githooks/parent/post-checkout"
chmod +x "${ROOT}/.githooks/child/post-commit"

git -C "${ROOT}" config core.hooksPath .githooks/parent
git -C "${ROOT}/${SUBMODULE_PATH}" config core.hooksPath "${ROOT}/.githooks/child"

echo "Installed parent post-checkout hook and child post-commit hook."
