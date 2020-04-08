package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class Label(
    @SerializedName("case") val case: String,
    @SerializedName("mode") val mode: String
)