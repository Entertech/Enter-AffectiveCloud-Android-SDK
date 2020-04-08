package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("sn") val sn: String
)