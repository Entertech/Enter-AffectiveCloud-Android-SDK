package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

interface IConnectionServiceListener {
    fun connectionSuccess(sessionId:String?)
    fun connectionError(error: Error?)
}