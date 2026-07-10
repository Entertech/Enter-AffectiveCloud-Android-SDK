#!/usr/bin/env bash
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
failures=0

require_text() {
    local file="$1"
    local expected="$2"
    if ! grep -Fq -- "$expected" "$ROOT_DIR/$file"; then
        printf 'FAIL: %s does not contain: %s\n' "$file" "$expected" >&2
        failures=$((failures + 1))
    fi
}

require_absent() {
    local file="$1"
    if [[ -e "$ROOT_DIR/$file" ]]; then
        printf 'FAIL: incompatible legacy file still exists: %s\n' "$file" >&2
        failures=$((failures + 1))
    fi
}

require_text "gradle/libs.versions.toml" 'agp = "8.13.2"'
require_text "gradle/libs.versions.toml" 'affective_sdk_api = "2.0.0-svm"'
require_text "gradle/wrapper/gradle-wrapper.properties" 'gradle-8.13-bin.zip'
require_text "build-logic/convention/src/main/kotlin/EnterAndroidLibraryConventionPlugin.kt" 'compileSdk = 36'
require_text "demo/build.gradle" 'compileSdkVersion 36'
require_text "demo/build.gradle" 'targetSdkVersion 36'
require_text "demo/build.gradle" 'minSdkVersion 24'
require_text "demo/build.gradle" "jvmTarget = '1.8'"
require_text "affective_local_sdk/build.gradle.kts" 'ndkVersion = "28.2.13676358"'
require_text "affective_local_sdk/CMakeLists.txt" '"-Wl,-z,max-page-size=16384"'
require_text "affective_local_sdk/CMakeLists.txt" '"-Wl,-z,common-page-size=16384"'
require_text "version.gradle" 'libraryVersion="2.0.0-svm"'
require_absent "demo/src/jniLibs/arm64-v8a/libaffective.so"

if (( failures > 0 )); then
    printf 'Android migration configuration failed with %d problem(s).\n' "$failures" >&2
    exit 1
fi

printf 'PASS: Android SDK 36 and 16 KB migration configuration is complete.\n'
