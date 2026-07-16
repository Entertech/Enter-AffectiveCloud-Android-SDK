package cn.entertech.biomoduledemo.affective

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DemoControlStateTest {
    @Test
    fun initialStateOnlyAllowsConnectInitializeAndAlgorithmSwitch() {
        val state = DemoControlState()

        assertTrue(state.controls.canConnectBle)
        assertTrue(state.controls.canInitializeAlgorithm)
        assertTrue(state.controls.canSwitchAlgorithm)

        assertFalse(state.controls.canDisconnectBle)
        assertFalse(state.controls.canStartUpload)
        assertFalse(state.controls.canPauseUpload)
        assertFalse(state.controls.canFinishAlgorithm)
        assertFalse(state.controls.canGetReport)
        assertFalse(state.controls.canRestoreAlgorithm)
    }

    @Test
    fun connectedBleDisablesConnectAndEnablesDisconnect() {
        val state = DemoControlState()

        state.onBleConnecting()
        assertFalse(state.controls.canConnectBle)
        assertFalse(state.controls.canDisconnectBle)

        state.onBleConnected()
        assertFalse(state.controls.canConnectBle)
        assertTrue(state.controls.canDisconnectBle)
    }

    @Test
    fun uploadRequiresBleConnectedAndAlgorithmInitialized() {
        val state = DemoControlState()

        state.onBleConnected()
        assertFalse(state.controls.canStartUpload)

        state.onAlgorithmInitializing()
        assertFalse(state.controls.canInitializeAlgorithm)
        state.onAlgorithmInitialized()

        assertTrue(state.controls.canStartUpload)
        assertTrue(state.controls.canFinishAlgorithm)
        assertTrue(state.controls.canGetReport)
        assertTrue(state.controls.canRestoreAlgorithm)
        assertFalse(state.controls.canInitializeAlgorithm)
        assertFalse(state.controls.canSwitchAlgorithm)
    }

    @Test
    fun uploadingDisablesRepeatedUploadAndDisconnectUntilPaused() {
        val state = DemoControlState()
        state.onBleConnected()
        state.onAlgorithmInitialized()

        state.onUploadStarted()

        assertFalse(state.controls.canStartUpload)
        assertTrue(state.controls.canPauseUpload)
        assertFalse(state.controls.canDisconnectBle)
        assertFalse(state.controls.canSwitchAlgorithm)

        state.onUploadStopped()

        assertTrue(state.controls.canStartUpload)
        assertFalse(state.controls.canPauseUpload)
        assertTrue(state.controls.canDisconnectBle)
    }

    @Test
    fun finishingAlgorithmStopsUploadAndUnlocksAlgorithmSwitchAfterFinished() {
        val state = DemoControlState()
        state.onBleConnected()
        state.onAlgorithmInitialized()
        state.onUploadStarted()

        state.onAlgorithmFinishing()

        assertFalse(state.controls.canStartUpload)
        assertFalse(state.controls.canPauseUpload)
        assertFalse(state.controls.canFinishAlgorithm)
        assertFalse(state.controls.canSwitchAlgorithm)

        state.onAlgorithmFinished()

        assertTrue(state.controls.canInitializeAlgorithm)
        assertTrue(state.controls.canSwitchAlgorithm)
        assertFalse(state.controls.canStartUpload)
    }

    @Test
    fun finishFailureKeepsAlgorithmInitializedAndAllowsRetryWithoutSwitching() {
        val state = DemoControlState()
        state.onBleConnected()
        state.onAlgorithmInitialized()
        state.onAlgorithmFinishing()

        state.onAlgorithmFinishFailed()

        assertTrue(state.controls.canFinishAlgorithm)
        assertTrue(state.controls.canGetReport)
        assertFalse(state.controls.canInitializeAlgorithm)
        assertFalse(state.controls.canSwitchAlgorithm)
    }

    @Test
    fun disconnectingBleStopsUploadAndDisablesUpload() {
        val state = DemoControlState()
        state.onBleConnected()
        state.onAlgorithmInitialized()
        state.onUploadStarted()

        state.onBleDisconnected()

        assertTrue(state.controls.canConnectBle)
        assertFalse(state.controls.canDisconnectBle)
        assertFalse(state.controls.canStartUpload)
        assertFalse(state.controls.canPauseUpload)
    }
}
