package cn.entertech.affective.data.provider.relaxation

import cn.entertech.affective.data.provider.BaseETCacheListDoubleDataProvider
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData

class RelaxationListCacheDataProvider(
    maxCacheSize: Int = 300, interceptData: (Double) -> Boolean = { data -> data == 0.0 }
) : BaseETCacheListDoubleDataProvider(maxCacheSize, interceptData) {

    override fun getTargetData(affectiveData: RealtimeAffectiveData?): Double {
        return affectiveData?.realtimeRelaxationData?.relaxation ?: 0.0
    }

}