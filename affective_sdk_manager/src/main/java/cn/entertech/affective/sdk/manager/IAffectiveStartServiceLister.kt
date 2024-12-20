package cn.entertech.affective.sdk.manager

interface IAffectiveStartServiceLister<Key> {
    fun startAffectionFail(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {

    }

    fun startBioFail(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {

    }

    fun startFail(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {

    }

    fun startSuccess(key: Key) {

    }

    fun hasStarted(key: Key)
}