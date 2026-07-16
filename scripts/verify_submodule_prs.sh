#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

BASE_REF=""
HEAD_REF="HEAD"
CHILD_BRANCH_PREFIX="algorithm-android-"

usage() {
    cat <<'EOF'
Usage: scripts/verify_submodule_prs.sh [--base-ref REF] [--head-ref REF]

Checks submodule pointer changes between base and head. For every changed
submodule, the script verifies that:

1. the submodule worktree is checked out at the recorded head commit;
2. the submodule head branch has been pushed to its upstream remote;
3. an open child repository PR exists from the submodule head branch to the
   mapped child base branch.

Branch mapping:
  parent dev_2.0 -> child algorithm-android-dev_2.0
EOF
}

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --base-ref)
            BASE_REF="${2:-}"
            shift 2
            ;;
        --head-ref)
            HEAD_REF="${2:-}"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            fail "unknown argument: $1"
            ;;
    esac
done

ref_exists() {
    git rev-parse --verify --quiet "$1^{commit}" >/dev/null
}

resolve_base_ref() {
    if [[ -n "${BASE_REF}" ]]; then
        if ref_exists "${BASE_REF}"; then
            printf '%s\n' "${BASE_REF}"
            return
        fi
        for remote in $(git remote); do
            if ref_exists "${remote}/${BASE_REF}"; then
                printf '%s\n' "${remote}/${BASE_REF}"
                return
            fi
        done
        fail "base ref not found: ${BASE_REF}"
    fi

    if [[ -n "${GITHUB_BASE_REF:-}" ]]; then
        if ref_exists "${GITHUB_BASE_REF}"; then
            printf '%s\n' "${GITHUB_BASE_REF}"
            return
        fi
        for remote in $(git remote); do
            if ref_exists "${remote}/${GITHUB_BASE_REF}"; then
                printf '%s\n' "${remote}/${GITHUB_BASE_REF}"
                return
            fi
        done
    fi

    if ref_exists "dev_2.0"; then
        printf '%s\n' "dev_2.0"
        return
    fi
    for remote in $(git remote); do
        if ref_exists "${remote}/dev_2.0"; then
            printf '%s\n' "${remote}/dev_2.0"
            return
        fi
    done

    fail "base ref not found; pass --base-ref or fetch dev_2.0"
}

short_branch_name() {
    local ref="$1"
    ref="${ref#refs/heads/}"
    if [[ "${ref}" == */* ]]; then
        local remote="${ref%%/*}"
        if git remote | grep -qx "${remote}"; then
            ref="${ref#*/}"
        fi
    fi
    printf '%s\n' "${ref}"
}

child_base_branch_for() {
    local parent_base
    parent_base="$(short_branch_name "$1")"
    if [[ "${parent_base}" == "${CHILD_BRANCH_PREFIX}"* ]]; then
        printf '%s\n' "${parent_base}"
    else
        printf '%s%s\n' "${CHILD_BRANCH_PREFIX}" "${parent_base}"
    fi
}

gitlink_sha() {
    local ref="$1"
    local path="$2"
    git ls-tree "${ref}" -- "${path}" | awk '$1 == "160000" { print $3 }'
}

submodule_paths() {
    git config --file .gitmodules --get-regexp '^submodule\..*\.path$' 2>/dev/null | awk '{ print $2 }'
}

upstream_remote_for() {
    local path="$1"
    local upstream
    upstream="$(git -C "${path}" rev-parse --abbrev-ref --symbolic-full-name '@{u}' 2>/dev/null || true)"
    if [[ -n "${upstream}" && "${upstream}" == */* ]]; then
        printf '%s\n' "${upstream%%/*}"
        return
    fi
    git -C "${path}" remote | head -n 1
}

verify_changed_submodule() {
    local path="$1"
    local base_sha="$2"
    local head_sha="$3"
    local child_base_branch="$4"

    if [[ ! -d "${path}/.git" && ! -f "${path}/.git" ]]; then
        fail "changed submodule is not initialized: ${path}"
    fi

    local child_head_sha
    child_head_sha="$(git -C "${path}" rev-parse HEAD)"
    if [[ "${child_head_sha}" != "${head_sha}" ]]; then
        fail "submodule ${path} is checked out at ${child_head_sha}, but parent records ${head_sha}"
    fi

    local child_head_branch
    child_head_branch="$(git -C "${path}" branch --show-current)"
    if [[ -z "${child_head_branch}" ]]; then
        fail "submodule ${path} is detached; checkout the branch that contains ${head_sha}"
    fi

    local child_remote
    child_remote="$(upstream_remote_for "${path}")"
    if [[ -z "${child_remote}" ]]; then
        fail "submodule ${path} has no remote; cannot verify pushed head branch"
    fi

    local remote_head_sha
    remote_head_sha="$(git -C "${path}" ls-remote --heads "${child_remote}" "${child_head_branch}" | awk '{ print $1 }')"
    if [[ -z "${remote_head_sha}" ]]; then
        fail "submodule ${path} branch ${child_head_branch} is not pushed to ${child_remote}"
    fi
    if [[ "${remote_head_sha}" != "${head_sha}" ]]; then
        fail "submodule ${path} remote branch ${child_head_branch} points to ${remote_head_sha}, but parent records ${head_sha}"
    fi

    local pr_count
    if ! pr_count="$(
        cd "${path}" && \
            gh pr list \
            --base "${child_base_branch}" \
            --head "${child_head_branch}" \
            --state open \
            --json number,url \
            --jq 'length'
    )"; then
        fail "failed to query child PR for ${path}; run gh auth status and retry"
    fi

    if [[ "${pr_count}" == "0" ]]; then
        cat >&2 <<EOF
Missing open PR for changed submodule ${path}.

Submodule base commit: ${base_sha}
Submodule head commit: ${head_sha}
Expected child PR:
  base: ${child_base_branch}
  head: ${child_head_branch}

Create it with:
  cd ${path}
  gh pr create --base ${child_base_branch} --head ${child_head_branch}
EOF
        exit 1
    fi

    printf 'PASS: changed submodule %s has open child PR (%s -> %s).\n' \
        "${path}" "${child_head_branch}" "${child_base_branch}"
}

BASE_REF_RESOLVED="$(resolve_base_ref)"
if ! ref_exists "${HEAD_REF}"; then
    fail "head ref not found: ${HEAD_REF}"
fi

PATHS=()
while IFS= read -r path; do
    [[ -n "${path}" ]] && PATHS+=("${path}")
done < <(submodule_paths)
if [[ "${#PATHS[@]}" -eq 0 ]]; then
    printf 'PASS: no submodules configured.\n'
    exit 0
fi

CHILD_BASE_BRANCH="$(child_base_branch_for "${BASE_REF_RESOLVED}")"
changed_count=0

for path in "${PATHS[@]}"; do
    base_sha="$(gitlink_sha "${BASE_REF_RESOLVED}" "${path}")"
    head_sha="$(gitlink_sha "${HEAD_REF}" "${path}")"
    if [[ "${base_sha}" == "${head_sha}" ]]; then
        continue
    fi
    changed_count=$((changed_count + 1))
    verify_changed_submodule "${path}" "${base_sha:-<missing>}" "${head_sha:-<missing>}" "${CHILD_BASE_BRANCH}"
done

if [[ "${changed_count}" -eq 0 ]]; then
    printf 'PASS: no submodule pointer changes between %s and %s.\n' "${BASE_REF_RESOLVED}" "${HEAD_REF}"
fi
