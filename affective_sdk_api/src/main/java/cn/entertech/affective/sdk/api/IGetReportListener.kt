package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.UploadReportEntity

/**
 * 获取报表接口
 * */
interface IGetReportListener {

    /**
     * 获取报表出错
     * */
    fun onError(error: Error?)


    /**
     * 获取报表成功
     * */
    fun onSuccess(entity: UploadReportEntity?)

    /**
     * 获取生物基础数据报表出错
     * */
    fun getBioReportError(error: Error?)


    /**
     * 获取生理状态分析数据报表出错
     * */
    fun getAffectiveReportError(error: Error?)
}