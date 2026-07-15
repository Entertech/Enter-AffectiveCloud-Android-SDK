#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

require_pattern() {
    local file="$1"
    local pattern="$2"
    local description="$3"
    grep -Eq "$pattern" "$file" || fail "$description ($file)"
}

require_pattern settings.gradle.kts \
    'include\(":compatibility-tests:legacy-consumer"\)' \
    'legacy compatibility consumer is not included'
require_pattern build.gradle.kts \
    'org\.jetbrains\.kotlinx\.binary-compatibility-validator' \
    'binary compatibility validation is not enabled'
require_pattern build.gradle.kts \
    'enter\.module-dependency-analysis' \
    'module dependency analysis plugin is not applied'
require_pattern build-logic/convention/build.gradle.kts \
    'enter\.module-dependency-analysis' \
    'module dependency analysis plugin is not registered'

for module in \
    affective_local_sdk \
    affective_cloud_sdk \
    affective_sdk_manager \
    affective_cloud_protocol \
    affective_data_provider; do
    require_pattern demo/build.gradle.kts \
        "project\\(\"${module}\"\)|project\\(\":${module}\"\\)" \
        "Demo does not directly depend on :${module}"
done

require_pattern .github/workflows/publish-pr-gate.yml \
    'verify_sdk_architecture\.sh' \
    'publish PR gate does not run the architecture verification'
require_pattern .github/workflows/release-publish.yml \
    'apiCheck analyzeModuleDependencies test' \
    'release workflow does not run ABI, dependency, and unit gates'

changed_files=()
if (( $# > 0 )); then
    changed_files=("$@")
else
    while IFS= read -r file; do
        [[ -n "$file" ]] && changed_files+=("$file")
    done < <(git diff --name-only HEAD --)
fi

for file in "${changed_files[@]}"; do
    case "$file" in
        affective_cloud_sdk/build.gradle|affective_cloud_sdk/build.gradle.kts)
            ;;
        affective_cloud_sdk|affective_cloud_sdk/*)
            fail "frozen affective_cloud_sdk was changed: $file"
            ;;
    esac
done

printf 'PASS: SDK architecture, compatibility, Demo dependency, and Cloud freeze checks passed.\n'
