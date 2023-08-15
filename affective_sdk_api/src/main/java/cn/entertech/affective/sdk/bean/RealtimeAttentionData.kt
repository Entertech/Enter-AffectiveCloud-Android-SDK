package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class RealtimeAttentionData(
    @SerializedName("attention") var attention: Double? = null
){
    override fun toString(): String {
        return "RealtimeAttentionData(attention=$attention)"
    }
}
