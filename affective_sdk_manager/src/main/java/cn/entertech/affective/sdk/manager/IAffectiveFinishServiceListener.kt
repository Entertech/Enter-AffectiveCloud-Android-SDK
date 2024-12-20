package cn.entertech.affective.sdk.manager

import cn.entertech.affective.sdk.bean.Error

interface IAffectiveFinishServiceListener<Key> {
    fun finishError(key: Key, error: Error?) {
    }

    fun finishSuccess(key: Key) {
    }
}