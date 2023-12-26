package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimeAttentionData(
    @SerializedName("attention") var attention: Double? = null
): Serializable {
    override fun toString(): String {
        return "RealtimeAttentionData(attention=$attention)"
    }
}
