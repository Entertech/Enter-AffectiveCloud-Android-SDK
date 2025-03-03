package cn.entertech.affective.sdk.manager

interface IAffectionConnectionServiceListener<Key> {

    fun connectionError(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {

    }

    fun connectionSuccess(key: Key, sessionId: String?) {

    }

    fun hasConnected(key: Key,sessionId: String){

    }
}