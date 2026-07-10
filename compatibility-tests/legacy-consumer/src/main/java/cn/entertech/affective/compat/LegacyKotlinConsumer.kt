package cn.entertech.affective.compat

import android.content.Context
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy

object LegacyKotlinConsumer {
    fun consume(service: IAffectiveDataAnalysisService, context: Context) {
        val config = EnterAffectiveConfigProxy(userId = "legacy", age = 18)
        service.connectAffectiveServiceConnection(null, config)
        service.checkEnvironment(context)
        service.appendEEGData(listOf(1, 2, 3), isEar = false)
        service.appendHeartRateData(72)
    }
}
