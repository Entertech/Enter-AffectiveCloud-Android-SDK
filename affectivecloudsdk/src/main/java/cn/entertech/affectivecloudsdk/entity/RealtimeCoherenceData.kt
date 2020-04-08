package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeCoherenceData(
    @SerializedName("coherence") var coherence: Double? = null
){
    override fun toString(): String {
        return "RealtimeCoherenceData(coherence=$coherence)"
    }
}
