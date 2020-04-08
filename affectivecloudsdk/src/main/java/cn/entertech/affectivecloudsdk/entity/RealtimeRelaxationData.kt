package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeRelaxationData(
    @SerializedName("relaxation") var relaxation: Double? = null
){

    override fun toString(): String {
        return "RealtimeRelaxationData(relaxation=$relaxation)"
    }
}
