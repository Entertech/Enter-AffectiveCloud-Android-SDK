package cn.entertech.affective.sdk.api

interface IAffectiveServiceConnectionListener {

    fun connectSuccess()

    fun connectFail()

    fun disconnect()

}