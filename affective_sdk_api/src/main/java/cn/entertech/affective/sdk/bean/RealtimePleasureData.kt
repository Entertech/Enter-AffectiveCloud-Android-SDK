package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimePleasureData(
    @SerializedName("pleasure") var pleasure: Double? = null
): Serializable {
    override fun toString(): String {
        return "RealtimePleasureData(pleasure=$pleasure)"
    }
}
