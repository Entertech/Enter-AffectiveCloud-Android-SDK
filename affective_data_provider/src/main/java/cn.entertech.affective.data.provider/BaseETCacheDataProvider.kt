package cn.entertech.affective.data.provider

abstract class BaseETCacheDataProvider<Cache, Data>(val interceptData: (Data) -> Boolean = { false }) :
    IDataProvider<Cache>, IDataIntercept<Data> {


    override fun intercept(data: Data): Boolean {
        return interceptData(data)
    }
}