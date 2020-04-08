package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimePressureData(
    @SerializedName("pressure") var pressure: Double? = null
){
    override fun toString(): String {
        return "RealtimePressureData(pressure=$pressure)"
    }
}
