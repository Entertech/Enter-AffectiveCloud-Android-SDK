package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.OptionalParamsMap

class StorageSettings internal constructor(builder: Builder) : OptionalParamsMap {

    enum class Sex(var value: String) {
        MALE("m"), FEMALE("f"),OTHER("o")
    }

    var storageSettings: HashMap<Any, Any>? = null
    override fun body(): HashMap<Any, Any>? {
        return storageSettings
    }

    init {
        storageSettings = builder.storageSettings
    }

    class Builder {
        internal var user: HashMap<Any, Any>? = null
        internal var data: HashMap<Any, Any>? = null
        internal var device: HashMap<Any, Any>? = null
        internal var label: HashMap<Any, Any>? = null
        internal var allow: Boolean = true
        internal var storageSettings: HashMap<Any, Any>? = null
        fun sex(sex: Sex): Builder {
            if (user == null) {
                user = HashMap()
            }
            user!!["sex"] = sex.value
            return this
        }

        fun age(age: Int): Builder {
            if (user == null) {
                user = HashMap()
            }
            user!!["age"] = age
            return this
        }

        fun sn(sn: String): Builder {
            if (device == null) {
                device = HashMap()
            }
            device!!["sn"] = sn
            return this
        }

        fun source(source: String): Builder {
            if (data == null) {
                data = HashMap()
            }
            data!!["source"] = source
            return this
        }

        fun mode(mode: List<Int>):Builder {
            if (label == null) {
                label = HashMap()
            }
            label!!["mode"] = mode
            return this
        }

        fun case(case: List<Int>):Builder {
            if (label == null) {
                label = HashMap()
            }
            label!!["case"] = case
            return this
        }

        fun allowStoreRawData(allow:Boolean):Builder{
            this.allow = allow
            return this
        }

        fun build(): StorageSettings {
            if (storageSettings == null) {
                storageSettings = HashMap()
            }
            if (user != null) {
                storageSettings!!["user"] = user!!
            }
            if (data != null) {
                storageSettings!!["data"] = data!!
            }
            if (device != null) {
                storageSettings!!["device"] = device!!
            }
            if (label != null) {
                storageSettings!!["label"] = label!!
            }
            storageSettings!!["allow"] = allow
            return StorageSettings(this)
        }
    }
}