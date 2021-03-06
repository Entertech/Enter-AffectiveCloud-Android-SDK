package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeArousalData(
    @SerializedName("arousal") var arousal: Double? = null
){
    override fun toString(): String {
        return "RealtimeArousalData(arousal=$arousal)"
    }
}
