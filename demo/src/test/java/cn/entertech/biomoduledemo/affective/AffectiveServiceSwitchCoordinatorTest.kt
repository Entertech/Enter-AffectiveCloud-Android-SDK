package cn.entertech.biomoduledemo.affective

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class AffectiveServiceSwitchCoordinatorTest {
    @Test
    fun cloudSwitchWithoutCredentialsDoesNotFinishOrReplaceCurrentService() {
        val local = FakeService("local")
        val callbacks = FakeCallbacks(local)
        val coordinator = AffectiveServiceSwitchCoordinator(
            initialMode = DemoAffectiveAlgorithmMode.Local,
            initialService = local,
            callbacks = callbacks,
        )
        var result: AffectiveServiceSwitchResult? = null

        coordinator.switchTo(
            targetMode = DemoAffectiveAlgorithmMode.Cloud,
            cloudCredential = DemoCloudCredential(appKey = "", appSecret = ""),
            onResult = { result = it },
        )

        assertEquals(AffectiveServiceSwitchResult.CloudConfigRequired, result)
        assertSame(local, coordinator.currentService)
        assertEquals(DemoAffectiveAlgorithmMode.Local, coordinator.currentMode)
        assertEquals(emptyList(), callbacks.events)
        assertNull(callbacks.pendingFinish)
    }

    @Test
    fun switchCreatesTargetServiceOnlyAfterCurrentServiceFinishesAndCloses() {
        val local = FakeService("local", started = true, connected = true)
        val callbacks = FakeCallbacks(local)
        val coordinator = AffectiveServiceSwitchCoordinator(
            initialMode = DemoAffectiveAlgorithmMode.Local,
            initialService = local,
            callbacks = callbacks,
        )
        val results = mutableListOf<AffectiveServiceSwitchResult>()

        coordinator.switchTo(
            targetMode = DemoAffectiveAlgorithmMode.Cloud,
            cloudCredential = DemoCloudCredential(appKey = "key", appSecret = "secret"),
            onResult = results::add,
        )

        assertEquals(listOf("finish:local"), callbacks.events)
        assertSame(local, coordinator.currentService)
        assertEquals(emptyList(), results)

        callbacks.pendingFinish?.finishSuccess()

        assertEquals(listOf("finish:local", "close:local", "create:Cloud"), callbacks.events)
        assertEquals(DemoAffectiveAlgorithmMode.Cloud, coordinator.currentMode)
        assertEquals("cloud", coordinator.currentService.name)
        assertEquals(
            listOf<AffectiveServiceSwitchResult>(
                AffectiveServiceSwitchResult.Switched(DemoAffectiveAlgorithmMode.Cloud),
            ),
            results,
        )
    }

    @Test
    fun finishFailureKeepsCurrentServiceAndDoesNotCreateTargetService() {
        val local = FakeService("local", started = true, connected = true)
        val callbacks = FakeCallbacks(local)
        val coordinator = AffectiveServiceSwitchCoordinator(
            initialMode = DemoAffectiveAlgorithmMode.Local,
            initialService = local,
            callbacks = callbacks,
        )
        val results = mutableListOf<AffectiveServiceSwitchResult>()

        coordinator.switchTo(
            targetMode = DemoAffectiveAlgorithmMode.Cloud,
            cloudCredential = DemoCloudCredential(appKey = "key", appSecret = "secret"),
            onResult = results::add,
        )
        callbacks.pendingFinish?.finishFailed("finish failed")

        assertEquals(listOf("finish:local"), callbacks.events)
        assertSame(local, coordinator.currentService)
        assertEquals(DemoAffectiveAlgorithmMode.Local, coordinator.currentMode)
        assertEquals(
            listOf<AffectiveServiceSwitchResult>(
                AffectiveServiceSwitchResult.FinishFailed("finish failed"),
            ),
            results,
        )
    }

    private data class FakeService(
        val name: String,
        var started: Boolean = false,
        var connected: Boolean = false,
    )

    private class FakeCallbacks(
        private val initialService: FakeService,
    ) : AffectiveServiceSwitchCallbacks<FakeService> {
        val events = mutableListOf<String>()
        var pendingFinish: AffectiveServiceFinishCallback? = null

        override fun createService(mode: DemoAffectiveAlgorithmMode): FakeService {
            events += "create:$mode"
            return when (mode) {
                DemoAffectiveAlgorithmMode.Local -> FakeService("local")
                DemoAffectiveAlgorithmMode.Cloud -> FakeService("cloud")
            }
        }

        override fun hasStarted(service: FakeService): Boolean = service.started

        override fun hasConnected(service: FakeService): Boolean = service.connected

        override fun finishService(service: FakeService, callback: AffectiveServiceFinishCallback) {
            events += "finish:${service.name}"
            pendingFinish = callback
        }

        override fun closeService(service: FakeService) {
            events += "close:${service.name}"
            service.connected = false
            service.started = false
        }

        init {
            require(initialService.name == "local")
        }
    }
}
