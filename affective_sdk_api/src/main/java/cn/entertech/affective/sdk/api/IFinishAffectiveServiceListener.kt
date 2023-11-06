package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

interface IFinishAffectiveServiceListener {
    fun finishBioFail(error: Error?)
    fun finishAffectiveFail(error: Error?)
    fun finishError(error: Error?)
    fun finishSuccess()


}