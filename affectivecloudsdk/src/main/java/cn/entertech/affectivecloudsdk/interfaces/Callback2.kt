package cn.entertech.affectivecloudsdk.interfaces

interface Callback2<T,K>{
    fun onSuccess(t:T?)
    fun onError(error:K?)
}