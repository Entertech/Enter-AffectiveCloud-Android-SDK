#!/bin/sh
set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

cd "${ROOT}"
git submodule update --init --recursive

"${ROOT}/scripts/install_submodule_branch_hooks.sh"

echo "Bootstrap complete."
