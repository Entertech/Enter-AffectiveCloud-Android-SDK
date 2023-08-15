package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class RealtimeFlowData(
    @SerializedName("meditation") var meditation: Double? = null,
    @SerializedName("meditation_tips") var meditationTips: Double? = null
){
    override fun toString(): String {
        return "RealtimeFlowData(meditation=$meditation, meditationTips=$meditationTips)"
    }
}
