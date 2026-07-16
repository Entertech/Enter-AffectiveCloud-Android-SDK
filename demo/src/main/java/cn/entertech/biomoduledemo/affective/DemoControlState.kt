package cn.entertech.biomoduledemo.affective

data class DemoControlSnapshot(
    val canConnectBle: Boolean,
    val canDisconnectBle: Boolean,
    val canInitializeAlgorithm: Boolean,
    val canStartUpload: Boolean,
    val canPauseUpload: Boolean,
    val canFinishAlgorithm: Boolean,
    val canGetReport: Boolean,
    val canRestoreAlgorithm: Boolean,
    val canSwitchAlgorithm: Boolean,
)

class DemoControlState {
    private var bleConnected = false
    private var bleConnecting = false
    private var algorithmInitialized = false
    private var algorithmInitializing = false
    private var algorithmFinishing = false
    private var uploading = false

    val controls: DemoControlSnapshot
        get() {
            val bleBusy = bleConnecting
            val algorithmBusy = algorithmInitializing || algorithmFinishing
            val canOperateInitializedAlgorithm = algorithmInitialized && !algorithmBusy
            return DemoControlSnapshot(
                canConnectBle = !bleConnected && !bleBusy && !uploading,
                canDisconnectBle = bleConnected && !bleBusy && !uploading,
                canInitializeAlgorithm = !algorithmInitialized && !algorithmBusy,
                canStartUpload = bleConnected && canOperateInitializedAlgorithm && !uploading,
                canPauseUpload = uploading,
                canFinishAlgorithm = algorithmInitialized && !algorithmBusy,
                canGetReport = canOperateInitializedAlgorithm,
                canRestoreAlgorithm = canOperateInitializedAlgorithm && !uploading,
                canSwitchAlgorithm = !algorithmInitialized && !algorithmBusy && !uploading,
            )
        }

    fun onBleConnecting() {
        bleConnecting = true
    }

    fun onBleConnected() {
        bleConnecting = false
        bleConnected = true
    }

    fun onBleDisconnected() {
        bleConnecting = false
        bleConnected = false
        uploading = false
    }

    fun onAlgorithmInitializing() {
        algorithmInitializing = true
    }

    fun onAlgorithmInitialized() {
        algorithmInitializing = false
        algorithmFinishing = false
        algorithmInitialized = true
    }

    fun onAlgorithmFinishing() {
        algorithmInitializing = false
        algorithmFinishing = true
        uploading = false
    }

    fun onAlgorithmFinished() {
        algorithmInitializing = false
        algorithmFinishing = false
        algorithmInitialized = false
        uploading = false
    }

    fun onAlgorithmFinishFailed() {
        algorithmInitializing = false
        algorithmFinishing = false
        algorithmInitialized = true
        uploading = false
    }

    fun onUploadStarted() {
        if (bleConnected && algorithmInitialized && !algorithmInitializing && !algorithmFinishing) {
            uploading = true
        }
    }

    fun onUploadStopped() {
        uploading = false
    }
}
