package cn.entertech.biomoduledemo.affective

enum class DemoAffectiveAlgorithmMode {
    Local,
    Cloud,
}

data class DemoCloudCredential(
    val appKey: String?,
    val appSecret: String?,
) {
    val isConfigured: Boolean
        get() = !appKey.isNullOrBlank() && !appSecret.isNullOrBlank()
}

sealed class AffectiveServiceSwitchResult {
    data class Switched(val mode: DemoAffectiveAlgorithmMode) : AffectiveServiceSwitchResult()
    data class AlreadySelected(val mode: DemoAffectiveAlgorithmMode) : AffectiveServiceSwitchResult()
    data class FinishFailed(val reason: String?) : AffectiveServiceSwitchResult()
    object CloudConfigRequired : AffectiveServiceSwitchResult()
    object SwitchingInProgress : AffectiveServiceSwitchResult()
}

interface AffectiveServiceFinishCallback {
    fun finishSuccess()
    fun finishFailed(reason: String?)
}

interface AffectiveServiceSwitchCallbacks<Service> {
    fun createService(mode: DemoAffectiveAlgorithmMode): Service
    fun hasStarted(service: Service): Boolean
    fun hasConnected(service: Service): Boolean
    fun finishService(service: Service, callback: AffectiveServiceFinishCallback)
    fun closeService(service: Service)
}

class AffectiveServiceSwitchCoordinator<Service>(
    initialMode: DemoAffectiveAlgorithmMode,
    initialService: Service,
    private val callbacks: AffectiveServiceSwitchCallbacks<Service>,
) {
    var currentMode: DemoAffectiveAlgorithmMode = initialMode
        private set

    var currentService: Service = initialService
        private set

    private var switching = false

    fun switchTo(
        targetMode: DemoAffectiveAlgorithmMode,
        cloudCredential: DemoCloudCredential = DemoCloudCredential(appKey = null, appSecret = null),
        onResult: (AffectiveServiceSwitchResult) -> Unit,
    ) {
        if (switching) {
            onResult(AffectiveServiceSwitchResult.SwitchingInProgress)
            return
        }
        if (targetMode == DemoAffectiveAlgorithmMode.Cloud && !cloudCredential.isConfigured) {
            onResult(AffectiveServiceSwitchResult.CloudConfigRequired)
            return
        }
        if (targetMode == currentMode) {
            onResult(AffectiveServiceSwitchResult.AlreadySelected(currentMode))
            return
        }

        val oldService = currentService
        switching = true

        fun completeSwitch() {
            if (callbacks.hasConnected(oldService)) {
                callbacks.closeService(oldService)
            }
            currentService = callbacks.createService(targetMode)
            currentMode = targetMode
            switching = false
            onResult(AffectiveServiceSwitchResult.Switched(targetMode))
        }

        if (callbacks.hasStarted(oldService)) {
            callbacks.finishService(
                oldService,
                object : AffectiveServiceFinishCallback {
                    override fun finishSuccess() {
                        completeSwitch()
                    }

                    override fun finishFailed(reason: String?) {
                        switching = false
                        onResult(AffectiveServiceSwitchResult.FinishFailed(reason))
                    }
                },
            )
        } else {
            completeSwitch()
        }
    }
}
