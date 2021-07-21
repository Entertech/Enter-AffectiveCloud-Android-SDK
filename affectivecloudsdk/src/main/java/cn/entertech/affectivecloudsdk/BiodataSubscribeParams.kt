package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.OptionalParamsList
import java.lang.IllegalStateException

class BiodataSubscribeParams internal constructor(builder: Builder) : OptionalParamsList {
    private var bioSubList: List<Any>? = null

    init {
        bioSubList = builder.bioSubList
    }

    override fun body(): List<Any>? {
        return bioSubList
    }

    class Builder {
        var bioSubList: ArrayList<String> = ArrayList()

        fun requestEEG(): Builder {
            bioSubList.add("eeg")
            return this
        }

        fun requestHR(): Builder {
            bioSubList.add("hr-v2")
            return this
        }

        fun build(): BiodataSubscribeParams {
            if (bioSubList.isEmpty()) {
                throw IllegalStateException("no biodata requested,pls call request..method before build")
            }
            return BiodataSubscribeParams(this)
        }
    }
}