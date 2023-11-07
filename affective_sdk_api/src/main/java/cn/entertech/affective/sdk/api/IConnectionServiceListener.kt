package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

interface IConnectionServiceListener {
    /**
     * 连接成功
     * @param sessionId sessionId
     * */
    fun connectionSuccess(sessionId:String?)

    /**
     * 连接失败
     * */
    fun connectionError(error: Error?)
}