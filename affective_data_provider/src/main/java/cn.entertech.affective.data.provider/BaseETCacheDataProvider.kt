package cn.entertech.affective.data.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseETCacheDataProvider<Cache, Data>(
    private val initData: Cache, val interceptData: (Data) -> Boolean = { false }
) : IDataProvider<Cache>, IDataIntercept<Data> {
    protected val _data = MutableStateFlow(initData)
    override val data: StateFlow<Cache> = _data.asStateFlow()


    override fun providerData(t: Cache, needLimit: Boolean) {
        _data.value = if (needLimit) {
            limitData(t)
        } else {
            t
        }
    }

    override fun intercept(data: Data): Boolean {
        return interceptData(data)
    }

    override fun clear() {
        _data.value = initData
    }
}
