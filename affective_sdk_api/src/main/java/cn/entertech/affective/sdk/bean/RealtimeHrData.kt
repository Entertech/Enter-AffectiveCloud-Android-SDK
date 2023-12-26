package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimeHrData(
    /**
     * 实时心率值,数值范围[0, 255]，单位：BPM
     * */
    @SerializedName("hr") var hr: Double? = null,
    /**
     * 实时心率变异性，数值范围[0, +∞)
     * */
    @SerializedName("hrv") var hrv: Double? = null
) : Serializable {
    override fun toString(): String {
        return "RealtimeHrData(hr=$hr, hrv=$hrv)"
    }
}
