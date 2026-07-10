# Affective SDK Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (- [ ]) syntax for tracking.

**Goal:** Refactor the SDK into independently consumable Local, Cloud, Manager, Protocol, and Data Provider components while preserving affective_sdk_api source and binary compatibility.

**Architecture:** Keep affective_sdk_api as the stable shared contract and keep the five product components directly consumable by applications. Refactor Manager around per-key AlgorithmSlot ownership, Local around testable lifecycle/buffer/subscription/native seams, Protocol around typed transport payloads, and Data Provider around selector/filter/window/state composition. affective_cloud_sdk remains frozen.

**Tech Stack:** Android Gradle Plugin 8.13.2, Gradle 8.13, Kotlin 1.9.0, Java 8/17 per existing module, JUnit 4, kotlinx-coroutines 1.6.4, Gson, JetBrains Binary Compatibility Validator 0.18.1.

## Global Constraints

- Do not modify any file under affective_cloud_sdk.
- Existing public/protected declarations in affective_sdk_api keep package, JVM descriptor, parameter names, defaults, visibility, nullability, and generated data-class shape.
- Do not add abstract members to IAffectiveDataAnalysisService.
- Existing callback API remains functional; deprecations use WARNING only.
- Demo may directly depend on Local, Cloud, Manager, Protocol, Data Provider, and API.
- Manager must not depend on Local or Cloud implementations.
- Protocol and Data Provider must not depend on Manager or algorithm implementations.
- Existing cloud-to-protocol dependency remains allowed.
- Every production behavior is introduced through a red-green-refactor cycle.
- Every commit message starts with [codex].
- Baseline evidence: ./gradlew test completed successfully with 541 executed tasks before implementation.

---

### Task 1: API compatibility baseline and dependency boundary gate

**Files:**

- Modify: build.gradle.kts
- Modify: gradle/libs.versions.toml
- Modify: affective_sdk_api/build.gradle.kts
- Modify: settings.gradle.kts
- Create: compatibility-tests/legacy-consumer/build.gradle.kts
- Create: compatibility-tests/legacy-consumer/src/main/AndroidManifest.xml
- Create: compatibility-tests/legacy-consumer/src/main/java/cn/entertech/affective/compat/LegacyKotlinConsumer.kt
- Create: compatibility-tests/legacy-consumer/src/main/java/cn/entertech/affective/compat/LegacyJavaConsumer.java
- Create: build-logic/convention/src/main/kotlin/ModuleDependencyAnalysisPlugin.kt
- Modify: build-logic/convention/build.gradle.kts
- Test: build-logic/convention/src/test/kotlin/ModuleDependencyRulesTest.kt
- Generated: affective_sdk_api/api/affective_sdk_api.api

**Interfaces:**

- Produces Gradle task apiCheck for affective_sdk_api.
- Produces root task analyzeModuleDependencies.
- Produces compile-only consumer module :compatibility-tests:legacy-consumer.

- [ ] **Step 1: Write failing module dependency rule tests**

Create ModuleDependencyRulesTest.kt with table-driven assertions:

~~~kotlin
class ModuleDependencyRulesTest {
    @Test
    fun allowsDocumentedEdges() {
        assertTrue(ModuleDependencyRules.isAllowed(":demo", ":affective_local_sdk"))
        assertTrue(ModuleDependencyRules.isAllowed(":affective_cloud_sdk", ":affective_cloud_protocol"))
        assertTrue(ModuleDependencyRules.isAllowed(":affective_sdk_manager", ":affective_sdk_api"))
    }

    @Test
    fun rejectsHorizontalAndReverseEdges() {
        assertFalse(ModuleDependencyRules.isAllowed(":affective_sdk_manager", ":affective_local_sdk"))
        assertFalse(ModuleDependencyRules.isAllowed(":affective_data_provider", ":affective_sdk_manager"))
        assertFalse(ModuleDependencyRules.isAllowed(":affective_sdk_api", ":affective_local_sdk"))
    }
}
~~~

- [ ] **Step 2: Run tests and verify RED**

Run:

~~~bash
./gradlew :build-logic:convention:test --tests ModuleDependencyRulesTest --console=plain
~~~

Expected: compilation fails because ModuleDependencyRules does not exist.

- [ ] **Step 3: Implement dependency rules and analysis task**

Implement ModuleDependencyRules with the exact allowed edge set from Global Constraints. Implement ModuleDependencyAnalysisPlugin so analyzeModuleDependencies inspects ProjectDependency entries from resolvable/declarable configurations, reports each forbidden project edge, and throws GradleException when violations exist.

- [ ] **Step 4: Add ABI validator and compatibility consumer**

Add BCV 0.18.1 at the root and ignore every project except affective_sdk_api. Add coroutines 1.6.4 to the version catalog. Change device in affective_sdk_api from implementation to api.

LegacyKotlinConsumer must compile the following legacy calls:

~~~kotlin
fun consume(service: IAffectiveDataAnalysisService, context: Context) {
    val config = EnterAffectiveConfigProxy(userId = "legacy", age = 18)
    service.connectAffectiveServiceConnection(null, config)
    service.checkEnvironment(context)
    service.appendEEGData(listOf(1, 2, 3), isEar = false)
    service.appendHeartRateData(72)
}
~~~

LegacyJavaConsumer must construct EnterAffectiveConfigProxy through an existing JVM-visible constructor and call getService, getSessionId, and closeAffectiveServiceConnection.

- [ ] **Step 5: Generate and inspect ABI baseline**

Run:

~~~bash
./gradlew apiDump --console=plain
./gradlew apiCheck :compatibility-tests:legacy-consumer:assembleDebug analyzeModuleDependencies --console=plain
~~~

Expected: BUILD SUCCESSFUL; only affective_sdk_api/api/affective_sdk_api.api is generated.

- [ ] **Step 6: Run regression suite**

Run:

~~~bash
./gradlew :affective_sdk_api:test :compatibility-tests:legacy-consumer:assembleDebug :build-logic:convention:test --console=plain
~~~

Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

~~~bash
git add build.gradle.kts gradle/libs.versions.toml settings.gradle.kts affective_sdk_api build-logic compatibility-tests
git commit -m "[codex] build: add SDK compatibility and dependency gates"
~~~

### Task 2: Manager per-key ownership and multi-algorithm dispatch

**Files:**

- Create: affective_sdk_manager/src/main/java/cn/entertech/affective/sdk/manager/internal/AlgorithmSlot.kt
- Create: affective_sdk_manager/src/main/java/cn/entertech/affective/sdk/manager/internal/AlgorithmRegistry.kt
- Modify: affective_sdk_manager/src/main/java/cn/entertech/affective/sdk/manager/AffectiveManager.kt
- Modify: affective_sdk_manager/build.gradle.kts
- Create: affective_sdk_manager/src/test/java/cn/entertech/affective/sdk/manager/FakeAffectiveService.kt
- Create: affective_sdk_manager/src/test/java/cn/entertech/affective/sdk/manager/AlgorithmRegistryTest.kt
- Create: affective_sdk_manager/src/test/java/cn/entertech/affective/sdk/manager/AffectiveManagerMultiAlgorithmTest.kt

**Interfaces:**

- Produces registerAffectiveService(key, service).
- Produces unregisterAffectiveService(key).
- Produces appendEEGDataToAll(data, isEar) and appendHeartRateDataToAll(data).
- Preserves all existing AffectiveManager entry points.

- [ ] **Step 1: Write failing registry tests**

Test that register returns the same service for a key, separate keys can hold Local and Cloud service fakes, and remove clears all slot-owned listener references.

~~~kotlin
@Test
fun storesIndependentAlgorithmSlots() {
    val registry = AlgorithmRegistry<String>()
    val local = FakeAffectiveService(AffectiveServiceWay.AffectiveLocalService)
    val cloud = FakeAffectiveService(AffectiveServiceWay.AffectiveCloudService)

    registry.register("local", local)
    registry.register("cloud", cloud)

    assertSame(local, registry.get("local")?.service)
    assertSame(cloud, registry.get("cloud")?.service)
}
~~~

- [ ] **Step 2: Run registry tests and verify RED**

Run:

~~~bash
./gradlew :affective_sdk_manager:testDebugUnitTest --tests "*AlgorithmRegistryTest" --console=plain
~~~

Expected: compilation fails because AlgorithmRegistry and AlgorithmSlot do not exist.

- [ ] **Step 3: Implement AlgorithmSlot and AlgorithmRegistry**

AlgorithmSlot owns:

~~~kotlin
internal class AlgorithmSlot(
    val service: IAffectiveDataAnalysisService,
) {
    var connectionListener: IConnectionServiceListener? = null
    var startListener: IStartAffectiveServiceLister? = null
    var finishListener: IFinishAffectiveServiceListener? = null
    var reportListener: IGetReportListener? = null
    var bioDataListener: ((RealtimeBioData?) -> Unit)? = null
    var affectiveDataListener: ((RealtimeAffectiveData?) -> Unit)? = null
}
~~~

AlgorithmRegistry uses ConcurrentHashMap and atomic get-or-create, register, remove, values, and clear.

- [ ] **Step 4: Write failing Manager behavior tests**

Cover:

- registerAffectiveService uses explicit instances without ServiceLoader.
- appendEEGDataToAll sends identical samples to both services.
- one service failure does not prevent the second service receiving input.
- finishService retains the slot until finishSuccess, then removes it.
- subscribe callbacks preserve their originating key.

- [ ] **Step 5: Run Manager tests and verify RED**

Run:

~~~bash
./gradlew :affective_sdk_manager:testDebugUnitTest --tests "*AffectiveManagerMultiAlgorithmTest" --console=plain
~~~

Expected: tests fail because registration/broadcast methods and delayed cleanup do not exist.

- [ ] **Step 6: Refactor AffectiveManager**

Replace per-key service/listener maps with AlgorithmRegistry and slot fields. Keep global observer lists. Explicit registration wins; initAffectiveService remains ServiceLoader fallback. Change finish cleanup to occur from finishSuccess or terminal error. Use suspendCancellableCoroutine with an atomic completion guard for new suspend bridges.

- [ ] **Step 7: Verify Manager**

Run:

~~~bash
./gradlew :affective_sdk_manager:test :affective_sdk_manager:lintDebug analyzeModuleDependencies --console=plain
~~~

Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

~~~bash
git add affective_sdk_manager
git commit -m "[codex] refactor: isolate manager algorithm slots"
~~~

### Task 3: Data Provider composition pipeline

**Files:**

- Create: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/pipeline/DataSelector.kt
- Create: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/pipeline/DataFilter.kt
- Create: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/pipeline/WindowPolicy.kt
- Create: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/pipeline/SlidingWindowPolicy.kt
- Create: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/pipeline/DisplayDataProvider.kt
- Modify: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/BaseETCacheDataProvider.kt
- Modify: affective_data_provider/src/main/java/cn.entertech.affective.data.provider/BaseETCacheListDoubleDataProvider.kt
- Modify: affective_data_provider/build.gradle.kts
- Create: affective_data_provider/src/test/java/cn/entertech/affective/data/provider/DisplayDataProviderTest.kt
- Create: affective_data_provider/src/test/java/cn/entertech/affective/data/provider/BaseETCacheListDoubleDataProviderTest.kt

**Interfaces:**

- Produces DisplayDataProvider<Input, Point>.
- Adds clear() to IDataProvider with a default no-op for compatibility outside affective_sdk_api.
- Existing Attention/Hr/Relaxation providers remain usable.

- [ ] **Step 1: Write failing window and provider tests**

~~~kotlin
@Test
fun keepsOnlyNewestPoints() {
    val provider = DisplayDataProvider(
        initial = emptyList<Int>(),
        selector = DataSelector<Int, Int> { it },
        filter = DataFilter { true },
        windowPolicy = SlidingWindowPolicy(3),
    )
    (1..5).forEach(provider::accept)
    assertEquals(listOf(3, 4, 5), provider.data.value)
}

@Test
fun clearResetsState() {
    provider.accept(1)
    provider.clear()
    assertTrue(provider.data.value.isEmpty())
}
~~~

Also add a regression test proving affective input respects maxCacheSize, which fails against current BaseETCacheListDoubleDataProvider.

- [ ] **Step 2: Run tests and verify RED**

Run:

~~~bash
./gradlew :affective_data_provider:testDebugUnitTest --tests "*DisplayDataProviderTest" --tests "*BaseETCacheListDoubleDataProviderTest" --console=plain
~~~

Expected: compilation failure for missing pipeline types, then behavior failure showing affective path exceeds maxCacheSize.

- [ ] **Step 3: Implement pipeline and fix legacy provider**

DisplayDataProvider holds one MutableStateFlow<List<Point>>, applies selector then filter then window policy, and emits immutable snapshots. SlidingWindowPolicy requires maxSize > 0 and returns the newest maxSize points. Apply limitData in both Bio and Affective legacy paths.

- [ ] **Step 4: Remove UI dependencies**

Remove AppCompat and Material from affective_data_provider. Add explicit coroutines-core 1.6.4 dependency and keep affective_sdk_api as api.

- [ ] **Step 5: Verify Data Provider**

Run:

~~~bash
./gradlew :affective_data_provider:test :affective_data_provider:lintDebug analyzeModuleDependencies --console=plain
~~~

Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

~~~bash
git add affective_data_provider gradle/libs.versions.toml
git commit -m "[codex] refactor: compose display data providers"
~~~

### Task 4: Typed realtime transport protocol

**Files:**

- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/ProtocolVersion.kt
- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/RealtimeTransportEnvelope.kt
- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/BioDataPayload.kt
- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/AffectiveDataPayload.kt
- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/RealtimeProtocolEncoder.kt
- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/GsonRealtimeProtocolEncoder.kt
- Create: affective_cloud_protocol/src/main/java/cn/entertech/affective/protocol/LegacyCloudProtocolEncoder.kt
- Create: affective_cloud_protocol/src/test/java/cn/entertech/affective/protocol/RealtimeProtocolEncoderTest.kt
- Create: affective_cloud_protocol/src/test/java/cn/entertech/affective/protocol/LegacyCloudProtocolEncoderTest.kt

**Interfaces:**

- Produces typed encodeBioData and encodeAffectiveData.
- Preserves Request, RequestBody, and AffectiveCloudResponseBody unchanged.

- [ ] **Step 1: Write failing typed encoder tests**

Assert a bio event produces envelope version "1", type "bio_data", timestamp supplied by the test, and a typed HR payload. Assert affective event produces attention/relaxation payload without Map<Any, Any>.

- [ ] **Step 2: Write failing legacy golden tests**

Use Gson JsonParser and structural JSON equality for the existing fields:

~~~json
{
  "code": 0,
  "request": {"services": "affective", "op": "subscribe"},
  "data": {
    "attention": {"attention": 0.8},
    "relaxation": {"relaxation": 0.6}
  },
  "msg": ""
}
~~~

- [ ] **Step 3: Run tests and verify RED**

Run:

~~~bash
./gradlew :affective_cloud_protocol:testDebugUnitTest --tests "*ProtocolEncoderTest" --console=plain
~~~

Expected: compilation fails because encoder and payload types do not exist.

- [ ] **Step 4: Implement typed and legacy encoders**

RealtimeTransportEnvelope is a regular generic class with version, type, timestampMillis, and payload. Payloads use typed nullable values and lists. GsonRealtimeProtocolEncoder maps public SDK beans into typed payloads. LegacyCloudProtocolEncoder delegates to existing AffectiveCloudResponseBody.obtain and serializes with Gson.

- [ ] **Step 5: Verify Protocol and frozen Cloud**

Run:

~~~bash
./gradlew :affective_cloud_protocol:test :affective_cloud_sdk:test :affective_cloud_sdk:assembleRelease analyzeModuleDependencies --console=plain
git diff --exit-code HEAD -- affective_cloud_sdk
~~~

Expected: BUILD SUCCESSFUL and no Cloud diff.

- [ ] **Step 6: Commit**

~~~bash
git add affective_cloud_protocol
git commit -m "[codex] feat: add typed realtime transport protocol"
~~~

### Task 5: Local lifecycle, subscription, buffer, and native seams

**Files:**

- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/internal/LocalAlgorithmState.kt
- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/internal/LocalSessionStateOwner.kt
- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/internal/LocalSubscriptionRegistry.kt
- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/internal/IntBatchBuffer.kt
- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/internal/NativeEngine.kt
- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/internal/JniNativeEngine.kt
- Modify: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/EnterAffectiveLocalService.kt
- Modify: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/EnterAffectiveSDKManager.kt
- Create: affective_local_sdk/src/test/java/cn/entertech/affective/IntBatchBufferTest.kt
- Create: affective_local_sdk/src/test/java/cn/entertech/affective/LocalSubscriptionRegistryTest.kt
- Create: affective_local_sdk/src/test/java/cn/entertech/affective/LocalSessionStateOwnerTest.kt

**Interfaces:**

- EnterAffectiveLocalService remains the ServiceLoader provider.
- NativeEngine is internal and injectable for tests.
- Existing flavor and artifact identifiers remain unchanged.

- [ ] **Step 1: Write failing buffer tests**

Verify IntBatchBuffer drains only when threshold is reached, returns values in order, clears after drain, and rejects non-positive thresholds.

- [ ] **Step 2: Write failing subscription and state tests**

Verify duplicate listeners are not added, removal stops delivery, clear removes all listeners, and state transitions reject Running before Connected/Starting.

- [ ] **Step 3: Run tests and verify RED**

Run:

~~~bash
./gradlew :affective_local_sdk:testSdkNoAuthDebugUnitTest --tests "*IntBatchBufferTest" --tests "*LocalSubscriptionRegistryTest" --tests "*LocalSessionStateOwnerTest" --console=plain
~~~

Expected: compilation fails because internal seam types do not exist.

- [ ] **Step 4: Implement internal seams**

IntBatchBuffer uses an ArrayList protected by synchronized methods. LocalSubscriptionRegistry owns all Bio/Affective/connect/disconnect listener lists. LocalSessionStateOwner exposes state and validated transition methods. NativeEngine lists only JNI operations currently used by EnterAffectiveSDKManager; JniNativeEngine delegates to JNI.getInstance().

- [ ] **Step 5: Integrate without changing public behavior**

EnterAffectiveLocalService delegates listener ownership and lifecycle flags to the new owners. EnterAffectiveSDKManager receives NativeEngine through an internal constructor and replaces raw CopyOnWriteArrayList batch logic with IntBatchBuffer. Preserve exact trigger thresholds and callback outputs.

- [ ] **Step 6: Verify all Local variants**

Run:

~~~bash
./gradlew :affective_local_sdk:test :affective_local_sdk:assembleRelease --console=plain
~~~

Then run:

~~~bash
find affective_local_sdk/build/outputs/aar -name "*.aar" -print0 | xargs -0 scripts/verify_android_16k.sh
~~~

Expected: all variant tests and AAR builds succeed; native verification passes.

- [ ] **Step 7: Commit**

~~~bash
git add affective_local_sdk
git commit -m "[codex] refactor: isolate local algorithm runtime seams"
~~~

### Task 6: Additive algorithm API, adapters, and Demo composition

**Files:**

- Create: affective_sdk_api/src/main/java/cn/entertech/affective/sdk/api/AffectiveAlgorithm.kt
- Create: affective_sdk_api/src/main/java/cn/entertech/affective/sdk/api/AffectiveAlgorithmState.kt
- Create: affective_sdk_api/src/main/java/cn/entertech/affective/sdk/api/AffectiveInput.kt
- Create: affective_sdk_api/src/main/java/cn/entertech/affective/sdk/api/AffectiveRealtimeEvent.kt
- Create: affective_sdk_api/src/main/java/cn/entertech/affective/sdk/api/AffectiveAlgorithmResult.kt
- Create: affective_sdk_manager/src/main/java/cn/entertech/affective/sdk/manager/LegacyAffectiveAlgorithmAdapter.kt
- Create: affective_local_sdk/src/main/java/cn/entertech/affectivesdk/manager/LocalAffectiveAlgorithm.kt
- Modify: demo/build.gradle
- Create: demo/src/main/java/cn/entertech/biomoduledemo/affective/AffectiveDemoPipeline.kt
- Create: demo/src/test/java/cn/entertech/biomoduledemo/affective/AffectiveDemoPipelineTest.kt
- Test: affective_sdk_manager/src/test/java/cn/entertech/affective/sdk/manager/LegacyAffectiveAlgorithmAdapterTest.kt

**Interfaces:**

- Adds only new affective_sdk_api declarations.
- Legacy API remains unchanged and operational.
- Cloud continues through LegacyAffectiveAlgorithmAdapter.

- [ ] **Step 1: Write failing adapter tests**

Verify connect/start/report/finish map legacy callbacks to AffectiveAlgorithmResult, realtime callbacks become keyed AffectiveRealtimeEvent values, and cancellation unregisters subscriptions.

- [ ] **Step 2: Run adapter tests and verify RED**

Run:

~~~bash
./gradlew :affective_sdk_manager:testDebugUnitTest --tests "*LegacyAffectiveAlgorithmAdapterTest" --console=plain
~~~

Expected: compilation fails because additive algorithm API and adapter do not exist.

- [ ] **Step 3: Implement additive API**

AffectiveAlgorithm exposes way, StateFlow<AffectiveAlgorithmState>, Flow<AffectiveRealtimeEvent>, suspend connect/start/report/finish, append(AffectiveInput), and disconnect. AffectiveInput is a sealed interface with EegBytes, EegSamples, HeartRate, Pepr, Bcg, and Gyro types. AffectiveAlgorithmResult is Success<T> or Failure(Error).

- [ ] **Step 4: Implement legacy and local adapters**

LegacyAffectiveAlgorithmAdapter wraps IAffectiveDataAnalysisService without modifying it. LocalAffectiveAlgorithm delegates to EnterAffectiveLocalService and exposes the new API.

- [ ] **Step 5: Write failing Demo pipeline test**

Verify one realtime event is both encoded through LegacyCloudProtocolEncoder and sent to the correct display provider.

- [ ] **Step 6: Implement Demo composition**

Add direct dependencies on Manager, Protocol, and Data Provider. AffectiveDemoPipeline accepts protocol encoder, attention/relaxation/hr providers, and outbound message callback; it contains no Android UI logic.

- [ ] **Step 7: Update ABI dump and verify additive-only diff**

Run:

~~~bash
./gradlew apiDump --console=plain
./gradlew apiCheck :affective_sdk_api:test :affective_sdk_manager:test :affective_local_sdk:test :demo:test --console=plain
~~~

Inspect the ABI diff and confirm existing lines are unchanged and only new types are added.

- [ ] **Step 8: Commit**

~~~bash
git add affective_sdk_api affective_sdk_manager affective_local_sdk demo
git commit -m "[codex] feat: add additive algorithm API and demo pipeline"
~~~

### Task 7: CI/release gates and final verification

**Files:**

- Modify: .github/workflows/publish-pr-gate.yml
- Modify: .github/workflows/release-publish.yml
- Create: scripts/verify_sdk_architecture.sh
- Create: scripts/verify_service_loader_artifacts.sh
- Modify: doc/tech/affective-sdk-refactoring-design.md

**Interfaces:**

- Publish gate runs ABI, architecture, unit, AAR metadata, ServiceLoader, and native checks before local publish.
- Cloud is still excluded from PUBLISH_MODULES.

- [ ] **Step 1: Write failing shell acceptance checks**

verify_sdk_architecture.sh must assert settings includes the compatibility consumer, build logic registers dependency analysis, API validation is enabled, Demo declares all five components, and Cloud is absent from the changed-file list supplied to the script.

verify_service_loader_artifacts.sh must inspect Local/Cloud AAR or compiled resource inputs and assert META-INF/services/cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService contains the expected implementation.

- [ ] **Step 2: Run checks and verify RED**

Run:

~~~bash
bash scripts/verify_sdk_architecture.sh
bash scripts/verify_service_loader_artifacts.sh
~~~

Expected: fail until workflow and artifact inputs are wired.

- [ ] **Step 3: Wire release gates**

Before Assemble release AARs, run:

~~~bash
./gradlew apiCheck analyzeModuleDependencies test --console=plain
bash scripts/verify_sdk_architecture.sh
~~~

After AAR collection, run ServiceLoader and 16 KB verification. Keep the existing five PUBLISH_MODULES unchanged and do not add affective_cloud_sdk.

- [ ] **Step 4: Update design implementation status**

Add an Implementation Status section mapping P0-P6 to concrete files and commands. Do not mark future Cloud refactor P7 complete.

- [ ] **Step 5: Run full verification**

Run:

~~~bash
./gradlew clean apiCheck analyzeModuleDependencies test assembleRelease :demo:assembleDebug --stacktrace --console=plain
bash scripts/verify_sdk_architecture.sh
find affective_local_sdk/build/outputs/aar -name "*.aar" -print0 | xargs -0 scripts/verify_android_16k.sh
bash scripts/verify_service_loader_artifacts.sh
git diff --exit-code HEAD -- affective_cloud_sdk
git diff --check
~~~

Expected: every command succeeds and Cloud has no diff.

- [ ] **Step 6: Commit**

~~~bash
git add .github scripts doc/tech/affective-sdk-refactoring-design.md
git commit -m "[codex] ci: enforce SDK refactoring release gates"
~~~

- [ ] **Step 7: Final branch audit**

Run:

~~~bash
git status --short --branch
git log --oneline --decorate -8
git diff HEAD~6..HEAD --stat
~~~

Expected: clean worktree, seven implementation commits plus this plan commit after the worktree setup commit, and no affective_cloud_sdk changes.
