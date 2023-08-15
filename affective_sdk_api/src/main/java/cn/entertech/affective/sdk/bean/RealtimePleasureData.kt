package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class RealtimePleasureData(
    @SerializedName("pleasure") var pleasure: Double? = null
){
    override fun toString(): String {
        return "RealtimePleasureData(pleasure=$pleasure)"
    }
}
