package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeHrData(
    @SerializedName("hr") var hr: Double? = null,
    @SerializedName("hrv") var hrv: ArrayList<Double>? = null
) {
    override fun toString(): String {
        return "RealtimeHrData(hr=$hr, hrv=$hrv)"
    }
}
