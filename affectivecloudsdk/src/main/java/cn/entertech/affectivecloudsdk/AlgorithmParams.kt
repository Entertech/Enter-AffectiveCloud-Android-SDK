package cn.entertech.affectivecloudsdk

class AlgorithmParams internal constructor(builder: Builder) {
    var algorithmParams: HashMap<Any, Any?>? = null

    init {
        algorithmParams = builder.algorithmParams
    }

    fun body(): HashMap<Any, Any?>? {
        return algorithmParams
    }

    class Builder {
        internal var algorithmParams: HashMap<Any, Any?> = HashMap()

        fun eeg(eeg: AlgorithmParamsEEG): Builder {
            algorithmParams["eeg"] = eeg.body()
            return this
        }

        fun build(): AlgorithmParams {
            return AlgorithmParams(this)
        }
    }
}