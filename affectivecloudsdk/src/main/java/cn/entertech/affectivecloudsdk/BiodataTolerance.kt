package cn.entertech.affectivecloudsdk

class BiodataTolerance internal constructor(builder: Builder) {
    var biodataTolerance: HashMap<Any, Any>? = null

    init {
        biodataTolerance = builder.biodataTolerance
    }

    fun body(): HashMap<Any, Any>? {
        return biodataTolerance
    }

    class Builder {
        internal var biodataTolerance: HashMap<Any, Any> = HashMap()

        /**
         * set eeg tolerance
         * @param value Int  range:0-4 default：2
         * @return Builder
         */
        fun eeg(value: Int): Builder {
            biodataTolerance["eeg"] = value
            return this
        }

        fun build(): BiodataTolerance {
            return BiodataTolerance(this)
        }
    }
}