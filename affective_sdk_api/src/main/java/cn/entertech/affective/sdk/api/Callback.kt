package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error


@Deprecated("")
interface Callback{
    fun onSuccess()
    fun onError(error: Error?)
    fun log(msg:String){}

}