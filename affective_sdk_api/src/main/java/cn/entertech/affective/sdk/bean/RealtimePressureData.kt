package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimePressureData(
    @SerializedName("pressure") var pressure: Double? = null
) : Serializable {
    override fun toString(): String {
        return "RealtimePressureData(pressure=$pressure)"
    }
}
