package cn.entertech.affective.sdk.utils

import android.util.Log

object LogUtil {
    fun d(tag:String,msg:String){
        Log.d(tag,msg)
    }

    fun i(tag:String,msg:String){
        Log.i(tag,msg)
    }

    fun e(tag: String,msg:String){
        Log.e(tag,msg)
    }
}