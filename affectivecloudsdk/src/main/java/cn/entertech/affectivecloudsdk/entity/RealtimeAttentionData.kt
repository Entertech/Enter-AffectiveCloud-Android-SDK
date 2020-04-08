package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeAttentionData(
    @SerializedName("attention") var attention: Double? = null
){
    override fun toString(): String {
        return "RealtimeAttentionData(attention=$attention)"
    }
}
