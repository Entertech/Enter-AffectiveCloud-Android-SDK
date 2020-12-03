package cn.entertech.affectivecloudsdk

class AlgorithmParamsEEG internal constructor(builder: Builder) {
    var algorithmParamsEEG: HashMap<Any, Any>? = null

    enum class Tolerance(var value: Int) {
        LEVEL_0(0), LEVEL_1(1), LEVEL_2(2), LEVEL_3(3), LEVEL_4(4)
    }

    enum class FilterMode(var value: String) {
        BASIC("basic"), SMART("smart"), HARD("hard")
    }

    enum class PowerMode(var value: String) {
        DB("db"), RATE("rate")
    }

    init {
        algorithmParamsEEG = builder.algorithmParamsEEG
    }

    fun body(): HashMap<Any, Any>? {
        return algorithmParamsEEG
    }

    class Builder {
        internal var algorithmParamsEEG: HashMap<Any, Any> = HashMap()

        fun tolerance(tolerance: Tolerance): Builder {
            algorithmParamsEEG["tolerance"] = tolerance.value
            return this
        }

        fun filterMode(filterMode: FilterMode): Builder {
            algorithmParamsEEG["filter_mode"] = filterMode.value
            return this
        }

        fun powerMode(powerMode: PowerMode): Builder {
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