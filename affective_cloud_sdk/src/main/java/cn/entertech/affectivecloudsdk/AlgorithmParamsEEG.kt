package cn.entertech.affectivecloudsdk

class AlgorithmParamsEEG internal constructor(builder: Builder) {
    var algorithmParamsEEG: HashMap<Any, Any>? = null


    init {
        algorithmParamsEEG = builder.algorithmParamsEEG
    }

    fun body(): HashMap<Any, Any>? {
        return algorithmParamsEEG
    }

    class Builder {
        internal var algorithmParamsEEG: HashMap<Any, Any> = HashMap()

        fun tolerance(tolerance: AlgorithmParams.Tolerance): Builder {
            algorithmParamsEEG["tolerance"] = tolerance.value
            return this
        }

        fun filterMode(filterMode: AlgorithmParams.FilterMode): Builder {
            algorithmParamsEEG["filter_mode"] = filterMode.value
            return this
        }

        fun powerMode(powerMode: AlgorithmParams.PowerMode): Builder {
            algorithmParamsEEG["power_mode"] = powerMode.value
            return this
        }

        fun channelPowerVerbose(flag: Boolean): Builder {
            algorithmParamsEEG["channel_power_verbose"] = flag
            return this
        }

        fun build(): AlgorithmParamsEEG {
            return AlgorithmParamsEEG(this)
        }
    }
}