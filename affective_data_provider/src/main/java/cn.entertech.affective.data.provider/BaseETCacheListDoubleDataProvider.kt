package cn.entertech.affective.data.provider

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

abstract class BaseETCacheListDoubleDataProvider(
    private val maxCacheSize: Int = 300,
    interceptData: (Double) -> Boolean = { data -> data == 0.0 }
) : BaseETCacheDataProvider<List<Double>, Double>(emptyList(), interceptData) {

    override fun providerData(affectiveData: RealtimeAffectiveData?) {
        super.providerData(affectiveData)
        val data = getTargetData(affectiveData)
        if (!intercept(data)) {
            val currentAttentionData = _data.value.toMutableList()
            currentAttentionData.add(data)
            _data.value = limitData(currentAttentionData)
        }
    }

    override fun providerData(bioData: RealtimeBioData?) {
        super.providerData(bioData)
        val data = getTargetData(bioData)
        if (!intercept(data)) {
            val currentAttentionData = _data.value.toMutableList()
            currentAttentionData.add(data)
            _data.value = limitData(currentAttentionData)
        }
    }

    open fun getTargetData(affectiveData: RealtimeAffectiveData?): Double = 0.0

    open fun getTargetData(bioData: RealtimeBioData?): Double = 0.0

    override fun limitData(t: List<Double>): List<Double> {
        if (t.size > maxCacheSize) {
            return t.subList(t.size - maxCacheSize, t.size)
        }
        return super.limitData(t)
    }
}
