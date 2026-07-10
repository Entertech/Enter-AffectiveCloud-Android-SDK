# Android 16 KB and SDK 36 Migration Design

## Goal

Upgrade the repository to the Android 16 (API 36) build toolchain and produce
`affective_local_sdk` artifacts whose native `libaffective.so` binaries support
16 KB memory page sizes. The release version will be `2.0.0-svm`. Maven
publication is outside this change.

## Current State and Root Cause

- The version catalog uses Android Gradle Plugin 8.6.0 and the wrapper uses
  Gradle 8.7.
- The shared library convention compiles against API 34, while the demo compiles
  and targets API 34.
- `affective_local_sdk` does not pin an NDK version. Existing CMake metadata
  shows that recent artifacts were built with NDK 26.1.10909125.
- `llvm-readelf` reports `0x1000` alignment for every `LOAD` segment in the
  current arm64-v8a `libaffective.so` output.
- `demo/src/jniLibs/arm64-v8a/libaffective.so` is an unused, nonstandard source
  path containing another `0x1000`-aligned binary.
- The demo does not declare a Kotlin JVM target. When Gradle runs on the
  repository's JDK 21 environment, Kotlin Gradle Plugin 1.9.0 attempts to use
  JVM target 21 and fails before compilation with `Unknown Kotlin JVM target:
  21`.
- The native source has no page-size constants or direct `mmap` usage requiring
  runtime code changes.

The root cause is therefore the native build toolchain and the absence of an
explicit alignment guarantee, rather than page-size-dependent application
logic.

During the first clean SDK 36 build, two pre-existing Kotlin source errors were
also exposed before AAR packaging: a duplicated
`startAffectiveService(IStartAffectiveServiceLister)` declaration and a call to
`kotlin.collections.ArrayDeque.addLast` that Kotlin 1.9.0 does not expose to
Kotlin callers. These are build prerequisites, not 16 KB runtime changes. The
minimal correction is to remove the identical duplicate declaration and use
the deque's public `add` operation, which appends to the tail and preserves the
existing cache order.

## Selected Approach

Use a current, mutually compatible Android toolchain and retain an explicit
linker-level safeguard:

- Android Gradle Plugin 8.13.2.
- Gradle wrapper 8.13.
- JDK 17 build compatibility.
- `compileSdk = 36` for every active library module.
- `compileSdk = 36` and `targetSdk = 36` for the demo application.
- Existing module-specific `minSdk` values remain unchanged.
- NDK r28c (`28.2.13676358`) is pinned for `affective_local_sdk`.
- CMake adds `-Wl,-z,max-page-size=16384` and
  `-Wl,-z,common-page-size=16384` to the `affective` shared-library target on
  Android.

NDK r28 already generates 16 KB-aligned libraries by default. The explicit
target link options document the compatibility requirement and prevent a
silent regression if the effective native toolchain changes.

## Repository Changes

### Toolchain configuration

- `gradle/libs.versions.toml`: change AGP from 8.6.0 to 8.13.2.
- `gradle/wrapper/gradle-wrapper.properties`: change the wrapper distribution
  from Gradle 8.7 to Gradle 8.13.
- `build-logic/convention/src/main/kotlin/EnterAndroidLibraryConventionPlugin.kt`:
  change the shared library `compileSdk` from 34 to 36 and keep existing
  `minSdk` and JVM compatibility behavior.
- `demo/build.gradle`: change `compileSdkVersion` and `targetSdkVersion` from 34
  to 36. Keep `minSdkVersion 24`, Java 1.8, and explicitly set Kotlin
  `jvmTarget` to 1.8 so the build does not inherit JDK 21 as its bytecode target.

### Native configuration

- `affective_local_sdk/build.gradle.kts`: pin `ndkVersion` to
  `28.2.13676358`.
- `affective_local_sdk/CMakeLists.txt`: apply the 16 KB maximum and common page
  size options only to the Android `affective` shared-library link step.
- Remove `demo/src/jniLibs/arm64-v8a/libaffective.so`. Gradle does not package
  this nonstandard directory, and retaining a known-incompatible binary would
  make repository-level compatibility checks misleading.

### Clean-build prerequisites

- `affective_sdk_api/src/main/java/cn/entertech/affective/sdk/api/IAffectiveDataAnalysisService.kt`:
  remove the second identical `startAffectiveService` declaration.
- `affective_sdk_api/src/main/java/cn/entertech/affective/sdk/BrainDataHelper.kt`:
  replace `sceegDataCache.addLast(value)` with `sceegDataCache.add(value)` to
  retain tail insertion through the Kotlin-visible mutable collection API.

### Release version

- `version.gradle`: change `libraryVersion` from `1.3.6-svm` to `2.0.0-svm`.
- `gradle/libs.versions.toml`: update the local `affective_sdk_api` version alias
  to `2.0.0-svm` so internal version declarations do not disagree.

No Maven publish, repository upload, tag, or consumer-project dependency change
is included.

## Verification Design

A reusable shell verifier will be added under `scripts/` with these behaviors:

1. Accept one or more APK, AAB, AAR, or `.so` paths.
2. Extract archive inputs into a temporary directory without modifying them.
3. Discover native shared libraries and inspect every ELF `LOAD` segment using
   an Android NDK `llvm-readelf` executable.
4. Require `LOAD` alignment of at least `0x4000` for arm64-v8a libraries.
5. For APK inputs, run Build Tools 36 `zipalign -v -c -P 16 4`.
6. Return a nonzero exit status and identify every incompatible file when a
   check fails.
7. Remove temporary files on both success and failure.

The verifier will first run against an existing 4 KB-aligned artifact to prove
that it fails for the expected reason. After the migration, verification will
cover:

- `affective_local_sdk` flowtime authentication Release AAR.
- `affective_local_sdk` flowtime no-authentication Release AAR.
- Demo Release APK.
- Gradle unit tests for active modules.

The final acceptance criteria are:

- Gradle reports version 8.13 and configures AGP 8.13.2 successfully.
- All active modules compile against SDK 36; the demo targets SDK 36.
- The demo Kotlin compile tasks target JVM 1.8 and do not inherit the Gradle
  runtime JDK version.
- Both flowtime Release AARs build successfully.
- `:affective_sdk_api:compileReleaseKotlin` succeeds without duplicate overload
  or unresolved deque API errors.
- Every arm64-v8a `libaffective.so` in those AARs has all `LOAD` alignments at
  least `0x4000`.
- The demo Release APK builds, passes 16 KB zip alignment, and contains no
  arm64-v8a native library with a `LOAD` alignment below `0x4000`.
- Automated unit tests pass.

## Error Handling and Diagnostics

The verification script will fail early for missing inputs, a missing Android
SDK, an unavailable ELF inspection tool, or an unavailable Build Tools 36
`zipalign`. Build failures and alignment failures remain distinct so a caller
can identify whether the problem is toolchain configuration, compilation, ELF
layout, or APK packaging.

## Out of Scope

- Publishing `2.0.0-svm` artifacts to local or remote Maven repositories.
- Updating `Enter-Flowtime-Android` to consume `2.0.0-svm`.
- Changing existing minimum SDK levels.
- Refactoring native algorithms or Android application behavior unrelated to
  SDK 36 or 16 KB compatibility.
- Resolving unrelated compiler, lint, or dependency deprecation warnings.
