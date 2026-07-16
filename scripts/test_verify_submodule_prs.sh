#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRIPT="${ROOT_DIR}/scripts/verify_submodule_prs.sh"
TEMP_DIR="$(mktemp -d)"

cleanup() {
    rm -rf "${TEMP_DIR}"
}
trap cleanup EXIT

git_init() {
    git init -q "$1"
    git -C "$1" config user.name "test"
    git -C "$1" config user.email "test@example.com"
}

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

CHILD_REMOTE="${TEMP_DIR}/child-remote.git"
CHILD_WORK="${TEMP_DIR}/child-work"
PARENT="${TEMP_DIR}/parent"
FAKE_BIN="${TEMP_DIR}/bin"

git init --bare -q "${CHILD_REMOTE}"
git_init "${CHILD_WORK}"

printf 'base\n' > "${CHILD_WORK}/algorithm.txt"
git -C "${CHILD_WORK}" add algorithm.txt
git -C "${CHILD_WORK}" commit -q -m "base"
BASE_SHA="$(git -C "${CHILD_WORK}" rev-parse HEAD)"
git -C "${CHILD_WORK}" branch algorithm-android-dev_2.0

printf 'head\n' > "${CHILD_WORK}/algorithm.txt"
git -C "${CHILD_WORK}" add algorithm.txt
git -C "${CHILD_WORK}" commit -q -m "head"
HEAD_SHA="$(git -C "${CHILD_WORK}" rev-parse HEAD)"
git -C "${CHILD_WORK}" branch algorithm-android-feature
git -C "${CHILD_WORK}" remote add origin "${CHILD_REMOTE}"
git -C "${CHILD_WORK}" push -q origin algorithm-android-dev_2.0 algorithm-android-feature
git --git-dir="${CHILD_REMOTE}" symbolic-ref HEAD refs/heads/algorithm-android-feature

git_init "${PARENT}"
mkdir -p "${PARENT}/affective_local_sdk/src"
cat > "${PARENT}/.gitmodules" <<EOF
[submodule "affective_local_sdk/src/main"]
	path = affective_local_sdk/src/main
	url = ${CHILD_REMOTE}
EOF
git -C "${PARENT}" add .gitmodules
git -C "${PARENT}" update-index --add --cacheinfo 160000,"${BASE_SHA}",affective_local_sdk/src/main
git -C "${PARENT}" commit -q -m "base"
git -C "${PARENT}" branch dev_2.0

git -C "${PARENT}" update-index --cacheinfo 160000,"${HEAD_SHA}",affective_local_sdk/src/main
git -C "${PARENT}" commit -q -m "head"

rm -rf "${PARENT}/affective_local_sdk/src/main"
git clone -q "${CHILD_REMOTE}" "${PARENT}/affective_local_sdk/src/main"
git -C "${PARENT}/affective_local_sdk/src/main" checkout -q algorithm-android-feature
git -C "${PARENT}/affective_local_sdk/src/main" branch --set-upstream-to=origin/algorithm-android-feature >/dev/null

mkdir -p "${FAKE_BIN}"
cat > "${FAKE_BIN}/gh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

if [[ "$1" == "pr" && "$2" == "list" ]]; then
    if [[ "${FAKE_GH_PR_COUNT:-0}" == "0" ]]; then
        printf '0\n'
    else
        printf '1\n'
    fi
    exit 0
fi

printf 'unexpected gh invocation: %s\n' "$*" >&2
exit 2
EOF
chmod +x "${FAKE_BIN}/gh"

set +e
missing_output="$(PATH="${FAKE_BIN}:${PATH}" FAKE_GH_PR_COUNT=0 "${SCRIPT}" --base-ref dev_2.0 2>&1)"
missing_status=$?
set -e

if [[ "${missing_status}" -eq 0 ]]; then
    fail "submodule PR verification passed without an open child PR"
fi
if [[ "${missing_output}" != *"Missing open PR"* ]]; then
    printf '%s\n' "${missing_output}" >&2
    fail "missing child PR failure did not explain the problem"
fi

PATH="${FAKE_BIN}:${PATH}" FAKE_GH_PR_COUNT=1 "${SCRIPT}" --base-ref dev_2.0 >/dev/null

printf 'PASS: verify_submodule_prs detects missing and existing child PRs.\n'
