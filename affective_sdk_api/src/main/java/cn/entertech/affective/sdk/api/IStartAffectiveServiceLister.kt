package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

interface IStartAffectiveServiceLister {
    /**
     * 启动成功
     * */
    fun startSuccess()

    /**
     * 启动生物基础服务失败
     * */
    fun startBioFail(error: Error?)

    /**
     * 启动生理基础服务失败
     * */
    fun startAffectionFail(error: Error?)

    /**
     * 启动失败
     * */
    fun startFail(error: Error?)
}