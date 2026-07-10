#!/usr/bin/env bash
set -uo pipefail

if (( $# == 0 )); then
    printf 'Usage: %s <apk|aab|aar|so> [...]\n' "$0" >&2
    exit 2
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$SDK_ROOT" && -f "$ROOT_DIR/local.properties" ]]; then
    SDK_ROOT="$(sed -n 's/^sdk.dir=//p' "$ROOT_DIR/local.properties" | tail -1)"
fi

find_llvm_readelf() {
    if [[ -n "${LLVM_READELF:-}" && -x "$LLVM_READELF" ]]; then
        printf '%s\n' "$LLVM_READELF"
        return 0
    fi

    local candidate
    local search_roots=()
    [[ -n "${ANDROID_NDK_HOME:-}" ]] && search_roots+=("$ANDROID_NDK_HOME")
    [[ -n "$SDK_ROOT" ]] && search_roots+=("$SDK_ROOT/ndk/28.2.13676358" "$SDK_ROOT/ndk")

    for candidate in "${search_roots[@]}"; do
        [[ -d "$candidate" ]] || continue
        candidate="$(find -L "$candidate" -path '*/toolchains/llvm/prebuilt/*/bin/llvm-readelf' -type f -perm -111 2>/dev/null | sort | tail -1)"
        if [[ -n "$candidate" ]]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    return 1
}

LLVM_READELF_BIN="$(find_llvm_readelf)" || {
    printf 'FAIL: cannot find an executable Android NDK llvm-readelf.\n' >&2
    exit 2
}

TEMP_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/verify-android-16k.XXXXXX")"
trap 'rm -rf "$TEMP_ROOT"' EXIT
failures=0

inspect_so() {
    local so="$1"
    local label="$2"
    local output
    local alignments
    local alignment

    if ! output="$("$LLVM_READELF_BIN" -lW "$so" 2>&1)"; then
        printf 'FAIL: llvm-readelf could not inspect %s\n%s\n' "$label" "$output" >&2
        failures=$((failures + 1))
        return
    fi

    alignments="$(printf '%s\n' "$output" | awk '$1 == "LOAD" {print $NF}')"
    if [[ -z "$alignments" ]]; then
        printf 'FAIL: no ELF LOAD segments found in %s\n' "$label" >&2
        failures=$((failures + 1))
        return
    fi

    while IFS= read -r alignment; do
        if (( alignment < 0x4000 )); then
            printf 'FAIL: %s has LOAD alignment %s; expected >= 0x4000\n' "$label" "$alignment" >&2
            failures=$((failures + 1))
        fi
    done <<< "$alignments"
}

verify_apk_zip_alignment() {
    local apk="$1"
    local zipalign_bin="${ZIPALIGN:-}"
    if [[ -z "$zipalign_bin" && -n "$SDK_ROOT" ]]; then
        zipalign_bin="$SDK_ROOT/build-tools/36.0.0/zipalign"
    fi
    if [[ ! -x "$zipalign_bin" ]]; then
        printf 'FAIL: Build Tools 36 zipalign is unavailable for %s\n' "$apk" >&2
        failures=$((failures + 1))
        return
    fi
    if ! "$zipalign_bin" -v -c -P 16 4 "$apk"; then
        printf 'FAIL: APK is not 16 KB zip-aligned: %s\n' "$apk" >&2
        failures=$((failures + 1))
    fi
}

input_index=0
for input in "$@"; do
    if [[ ! -f "$input" ]]; then
        printf 'FAIL: input does not exist: %s\n' "$input" >&2
        failures=$((failures + 1))
        continue
    fi

    case "${input##*.}" in
        so)
            inspect_so "$input" "$input"
            ;;
        aar|aab|apk)
            input_index=$((input_index + 1))
            extract_dir="$TEMP_ROOT/input-$input_index"
            mkdir -p "$extract_dir"
            if ! unzip -qq "$input" -d "$extract_dir"; then
                printf 'FAIL: cannot extract archive: %s\n' "$input" >&2
                failures=$((failures + 1))
                continue
            fi

            native_count=0
            while IFS= read -r -d '' so; do
                case "$so" in
                    */arm64-v8a/*.so)
                        native_count=$((native_count + 1))
                        inspect_so "$so" "$input!/${so#"$extract_dir"/}"
                        ;;
                esac
            done < <(find "$extract_dir" -type f -name '*.so' -print0)

            if (( native_count == 0 )); then
                printf 'FAIL: no arm64-v8a native libraries found in %s\n' "$input" >&2
                failures=$((failures + 1))
            fi

            if [[ "${input##*.}" == "apk" ]]; then
                verify_apk_zip_alignment "$input"
            fi
            ;;
        *)
            printf 'FAIL: unsupported input type: %s\n' "$input" >&2
            failures=$((failures + 1))
            ;;
    esac
done

if (( failures > 0 )); then
    printf 'Android 16 KB verification failed with %d problem(s).\n' "$failures" >&2
    exit 1
fi

printf 'PASS: all arm64-v8a LOAD segments and APK alignments are 16 KB compatible.\n'
