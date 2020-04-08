package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeBioData(
    @SerializedName("realtimeEEGData") var realtimeEEGData: RealtimeEEGData? = null,
    var realtimeHrData: RealtimeHrData? = null
) {
    override fun toString(): String {
        return "RealtimeBioData(realtimeEEGData=$realtimeEEGData, realtimeHrData=$realtimeHrData)"
    }
}
