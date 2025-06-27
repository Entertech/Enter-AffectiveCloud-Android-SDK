package cn.entertech.affective.data.provider.attention

import cn.entertech.affective.data.provider.BaseETCacheListDoubleDataProvider
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData

class AttentionListCacheDataProvider(
    maxCacheSize: Int = 300, interceptData: (Double) -> Boolean = { data -> data == 0.0 }
) : BaseETCacheListDoubleDataProvider(maxCacheSize, interceptData) {

    override fun getTargetData(affectiveData: RealtimeAffectiveData?): Double {
        return affectiveData?.realtimeAttentionData?.attention ?: 0.0
    }

}