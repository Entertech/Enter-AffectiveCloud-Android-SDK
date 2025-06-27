package cn.entertech.affective.data.provider

interface IDataIntercept<Data> {

    /**
     * 拦截数据
     * @return true 拦截，false 不拦截
     * */
    fun intercept(data: Data):Boolean
}