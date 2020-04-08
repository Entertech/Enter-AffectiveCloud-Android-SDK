package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RequestBody(@SerializedName("services") val services: String, @SerializedName("op") val op: String, @SerializedName("kwargs") val kwargs: HashMap<Any, Any>?)