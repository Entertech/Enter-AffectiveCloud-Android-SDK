package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimeArousalData(
    @SerializedName("arousal") var arousal: Double? = null
): Serializable {
    override fun toString(): String {
        return "RealtimeArousalData(arousal=$arousal)"
    }
}
