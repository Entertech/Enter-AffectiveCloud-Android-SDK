package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealtimeRelaxationData(
    @SerializedName("relaxation") var relaxation: Double? = null
): Serializable {

    override fun toString(): String {
        return "RealtimeRelaxationData(relaxation=$relaxation)"
    }
}
