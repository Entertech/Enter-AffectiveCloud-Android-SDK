#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SERVICE_PATH='META-INF/services/cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService'
LOCAL_PROVIDER='cn.entertech.affectivesdk.manager.EnterAffectiveLocalService'
CLOUD_PROVIDER='cn.entertech.affectivecloudsdk.EnterAffectiveCloudService'
TEMP_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/verify-service-loader.XXXXXX")"
trap 'rm -rf "$TEMP_ROOT"' EXIT

aars=()
if (( $# > 0 )); then
    aars=("$@")
else
    while IFS= read -r aar; do
        aars+=("$aar")
    done < <(
        find affective_local_sdk/build/outputs/aar affective_cloud_sdk/build/outputs/aar \
            -maxdepth 1 -type f -name '*.aar' -print 2>/dev/null | LC_ALL=C sort
    )
fi

(( ${#aars[@]} > 0 )) || {
    printf 'FAIL: no AAR inputs were found for ServiceLoader verification.\n' >&2
    exit 1
}

local_count=0
cloud_count=0
failures=0
index=0

verify_provider() {
    local aar="$1"
    local expected="$2"
    local classes_jar="$3"
    local actual

    if ! unzip -p "$aar" classes.jar > "$classes_jar"; then
        printf 'FAIL: cannot extract classes.jar from %s\n' "$aar" >&2
        failures=$((failures + 1))
        return
    fi
    if ! actual="$(unzip -p "$classes_jar" "$SERVICE_PATH" 2>/dev/null)"; then
        printf 'FAIL: %s is missing %s\n' "$aar" "$SERVICE_PATH" >&2
        failures=$((failures + 1))
        return
    fi
    if ! printf '%s\n' "$actual" | grep -Fxq "$expected"; then
        printf 'FAIL: %s does not register %s\n' "$aar" "$expected" >&2
        failures=$((failures + 1))
    fi
}

for aar in "${aars[@]}"; do
    [[ -f "$aar" ]] || {
        printf 'FAIL: AAR input does not exist: %s\n' "$aar" >&2
        failures=$((failures + 1))
        continue
    }
    index=$((index + 1))
    case "$aar" in
        *affective_local_sdk*|*affective-offline*)
            local_count=$((local_count + 1))
            verify_provider "$aar" "$LOCAL_PROVIDER" "$TEMP_ROOT/local-$index.jar"
            ;;
        *affective_cloud_sdk*|*affective-cloud-sdk*)
            cloud_count=$((cloud_count + 1))
            verify_provider "$aar" "$CLOUD_PROVIDER" "$TEMP_ROOT/cloud-$index.jar"
            ;;
    esac
done

if (( local_count == 0 )); then
    printf 'FAIL: no Local SDK AAR was supplied.\n' >&2
    failures=$((failures + 1))
fi
if (( cloud_count == 0 )); then
    printf 'FAIL: no Cloud SDK AAR was supplied.\n' >&2
    failures=$((failures + 1))
fi
if (( failures > 0 )); then
    printf 'ServiceLoader verification failed with %d problem(s).\n' "$failures" >&2
    exit 1
fi

printf 'PASS: Local and Cloud AARs preserve their ServiceLoader providers.\n'
