package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

interface IStartAffectiveServiceLister {
    fun startSuccess()

    fun startBioFail(error: Error?)

    fun startAffectionFail(error: Error?)

    fun startFail(error: Error?)
}