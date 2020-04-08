package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("age") var age: Int,
    @SerializedName("sex") var sex: String
)