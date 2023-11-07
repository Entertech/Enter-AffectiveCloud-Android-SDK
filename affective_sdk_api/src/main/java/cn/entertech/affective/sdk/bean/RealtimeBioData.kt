package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class RealtimeBioData(
    /**
     * eeg数据
     * */
    @SerializedName("realtimeEEGData") var realtimeEEGData: RealtimeEEGData? = null,
    var realtimeHrData: RealtimeHrData? = null,
    var realtimeMCEEGData: RealtimeMCEEGData? = null,
    var realtimeBCGData: RealtimeBCGData? = null,
    var realtimePEPRData: RealtimePEPRData? = null,
    var realtimeDceegSsvepData: RealtimeDceegSsvepData? = null
) {
    override fun toString(): String {
        return "RealtimeBioData(realtimeEEGData=$realtimeEEGData, realtimeHrData=$realtimeHrData, realtimeMCEEGData=$realtimeMCEEGData, realtimeBCGData=$realtimeBCGData, realtimePEPRData=$realtimePEPRData, realtimeDceegSsvepData=$realtimeDceegSsvepData)"
    }
}
