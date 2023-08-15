package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class RealtimePressureData(
    @SerializedName("pressure") var pressure: Double? = null
){
    override fun toString(): String {
        return "RealtimePressureData(pressure=$pressure)"
    }
}
