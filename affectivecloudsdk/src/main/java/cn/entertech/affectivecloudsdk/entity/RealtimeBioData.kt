package cn.entertech.affectivecloudsdk.entity

data class RealtimeBioData(
    var realtimeEEGData: RealtimeEEGData? = null,
    var realtimeHrData: RealtimeHrData? = null
) {
}
