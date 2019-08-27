package cn.entertech.affectivecloudsdk.interfaces

interface Observable<T, K> {
    fun subscribe(observer: Observer<T, K>)
    fun unsubscribe(callback: Callback2<K>)
}