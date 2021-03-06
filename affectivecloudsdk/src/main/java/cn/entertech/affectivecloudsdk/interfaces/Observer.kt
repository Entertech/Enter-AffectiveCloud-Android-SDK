package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affectivecloudsdk.entity.Error

interface Observer<T, K> {
    fun onRealtimeDataResponseSuccess(data: T?)
    fun onRealtimeDataResponseError(error: Error?)
    fun onSubscribeSuccess(subField: K?)
    fun onSubscribeError(error: Error?)
}