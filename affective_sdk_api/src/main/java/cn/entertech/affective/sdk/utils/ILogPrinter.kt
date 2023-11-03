package cn.entertech.affective.sdk.utils

interface ILogPrinter {

    fun d(tag: String, msg: String)

    fun i(tag: String, msg: String)

    fun e(tag: String, msg: String)
}