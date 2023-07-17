package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affectivecloudsdk.entity.Error

interface Callback{
    fun onSuccess()
    fun onError(error:Error?)
    fun log(msg:String){}

}