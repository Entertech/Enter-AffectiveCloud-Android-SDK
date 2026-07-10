package cn.entertech.affective.sdk.manager.internal

import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IConnectionServiceListener
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IGetReportListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

internal class AlgorithmSlot(
    val service: IAffectiveDataAnalysisService,
) {
    var connectionListener: IConnectionServiceListener? = null
    var startListener: IStartAffectiveServiceLister? = null
    var finishListener: IFinishAffectiveServiceListener? = null
    var reportListener: IGetReportListener? = null
    var bioDataListener: ((RealtimeBioData?) -> Unit)? = null
    var affectiveDataListener: ((RealtimeAffectiveData?) -> Unit)? = null

    fun clearListeners() {
        connectionListener = null
        startListener = null
        finishListener = null
        reportListener = null
        bioDataListener = null
        affectiveDataListener = null
    }
}
