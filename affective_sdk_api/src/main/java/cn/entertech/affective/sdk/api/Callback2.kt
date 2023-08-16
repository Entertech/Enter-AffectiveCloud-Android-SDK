package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

interface Callback2<T>{
    fun onSuccess(t:T?)
    fun onError(error: Error?)
    fun log(msg:String){}
}