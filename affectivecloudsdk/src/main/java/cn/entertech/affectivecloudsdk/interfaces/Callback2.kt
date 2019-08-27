package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affectivecloudsdk.entity.Error

interface Callback2<T>{
    fun onSuccess(t:T?)
    fun onError(error:Error?)
}