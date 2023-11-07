package cn.entertech.affective.sdk.utils

import android.util.Log

object DefaultLogPrinter:ILogPrinter {
    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }
}