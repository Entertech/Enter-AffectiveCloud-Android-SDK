package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affective.sdk.api.Callback2

interface Observable<T, K> {
    fun subscribe(observer: Observer<T, K>)
    fun unsubscribe(callback: Callback2<K>)
}