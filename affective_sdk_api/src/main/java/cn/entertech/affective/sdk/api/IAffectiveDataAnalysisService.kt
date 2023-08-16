package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

interface IAffectiveDataAnalysisService {

    /**
     * 启动情感服务
     * */
    fun startAffectiveService()

    /**
     * 重启情感服务
     * */
    fun restoreAffectiveService()

    /**
     * 订阅数据
     * */
    fun subscribeData()

    /**
     * 取消订阅
     * */
    fun unSubscribeData()
    /**
     * 发送数据
     * */
    fun uploadData()
    /**
     * 结束情感服务
     * */
    fun closeAffectiveService()


}