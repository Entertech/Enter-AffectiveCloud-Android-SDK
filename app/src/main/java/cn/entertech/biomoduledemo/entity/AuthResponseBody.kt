package cn.entertech.biomoduledemo.entity

data class AuthResponseBody(
    val `data`: Data,
    val code: Int,
    val msg: String,
    val request: Request
)

data class Data(
    val session_id: String
)

data class Request(
    val op: String,
    val services: String
)