package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimeCoherenceData(
    @SerializedName("coherence") var coherence: Double? = null
): Serializable {
    override fun toString(): String {
        return "RealtimeCoherenceData(coherence=$coherence)"
    }
}
