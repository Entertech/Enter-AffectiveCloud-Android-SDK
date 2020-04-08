package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimePleasureData(
    @SerializedName("pleasure") var pleasure: Double? = null
){
    override fun toString(): String {
        return "RealtimePleasureData(pleasure=$pleasure)"
    }
}
