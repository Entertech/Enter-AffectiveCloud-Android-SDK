package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeBioData(
    @SerializedName("realtimeEEGData") var realtimeEEGData: RealtimeEEGData? = null,
    var realtimeHrData: RealtimeHrData? = null,
    var realtimeMCEEGData: RealtimeMCEEGData? = null,
    var realtimeBCGData: RealtimeBCGData? = null,
    var realtimePEPRData: RealtimePEPRData? = null
) {
    override fun toString(): String {
        return "RealtimeBioData(realtimeEEGData=$realtimeEEGData, realtimeHrData=$realtimeHrData, realtimeMCEEGData=$realtimeMCEEGData, realtimeBCGData=$realtimeBCGData, realtimePEPRData=$realtimePEPRData)"
    }
}
