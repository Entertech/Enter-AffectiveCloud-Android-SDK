package cn.entertech.affective.data.provider

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import kotlinx.coroutines.flow.StateFlow

interface IDataProvider<T> {

    val data: StateFlow<T>


    fun providerData(bioData: RealtimeBioData?) {

    }

    fun providerData(affectiveData: RealtimeAffectiveData?) {

    }

    fun limitData(t:T):T{
        return t
    }

}