package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.UploadReportEntity

interface IGetReportListener {

    fun onError(error: Error?)

    fun onSuccess(entity: UploadReportEntity?=null)

    fun getBioReportError(error: Error?)

    fun getAffectiveReportError(error: Error?)
}