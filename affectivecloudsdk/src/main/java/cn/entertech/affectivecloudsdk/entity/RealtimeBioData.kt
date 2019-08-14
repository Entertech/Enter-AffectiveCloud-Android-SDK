package cn.entertech.affectivecloudsdk.entity

data class RealtimeBioData(
    var realtimeEEGData: RealtimeEEGData? = null,
    var realtimeHrData: RealtimeHrData? = null
) {
    override fun toString(): String {
        return "RealtimeBioData(realtimeEEGData=$realtimeEEGData, realtimeHrData=$realtimeHrData)"
    }
}
