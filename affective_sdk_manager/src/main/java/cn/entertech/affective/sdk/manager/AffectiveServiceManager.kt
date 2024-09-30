package cn.entertech.affective.sdk.manager

import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import java.util.concurrent.ConcurrentHashMap

class AffectiveServiceManager<Key> private constructor() {

    private val managerMap by lazy {
        ConcurrentHashMap<Key, IAffectiveDataAnalysisService?>()
    }

    private val bioDataListenerMap by lazy {
        ConcurrentHashMap<Key, ((RealtimeBioData?) -> Unit)?>()
    }

    private val affectiveDataListenerMap by lazy {
        HashMap<Key, ((RealtimeAffectiveData?) -> Unit)?>()
    }

    private val startAffectiveServiceListerMap by lazy {
        HashMap<Key, IStartAffectiveServiceLister?>()
    }


    fun startAffectiveService(
        key: Key,
        bioDataListener: (RealtimeBioData?) -> Unit,
        affectiveDataListener: (RealtimeAffectiveData?) -> Unit
    ) {
        bioDataListenerMap[key] = bioDataListener
        affectiveDataListenerMap[key] = affectiveDataListener
        managerMap[key]?.let {
            it.subscribeData(bioDataListener, affectiveDataListener)
        }
    }



}