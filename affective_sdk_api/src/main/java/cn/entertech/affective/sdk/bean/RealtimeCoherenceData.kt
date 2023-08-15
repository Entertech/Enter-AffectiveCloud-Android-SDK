package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class RealtimeCoherenceData(
    @SerializedName("coherence") var coherence: Double? = null
){
    override fun toString(): String {
        return "RealtimeCoherenceData(coherence=$coherence)"
    }
}
