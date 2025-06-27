package cn.entertech.affective.data.provider.hr

import cn.entertech.affective.data.provider.BaseETCacheListDoubleDataProvider
import cn.entertech.affective.sdk.bean.RealtimeBioData

class HrListCacheDataProvider(
    maxCacheSize: Int = 300, interceptData: (Double) -> Boolean = { data -> data == 0.0 }
) : BaseETCacheListDoubleDataProvider(maxCacheSize, interceptData) {


    override fun getTargetData(bioData: RealtimeBioData?): Double {
        return bioData?.realtimeHrData?.hr ?: 0.0
    }

}