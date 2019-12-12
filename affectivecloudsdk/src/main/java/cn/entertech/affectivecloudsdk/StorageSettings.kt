package cn.entertech.affectivecloudsdk

import cn.entertech.biomoduledemo.entity.Data
import cn.entertech.biomoduledemo.entity.Device
import cn.entertech.biomoduledemo.entity.Label
import cn.entertech.biomoduledemo.entity.User

class StorageSettings internal constructor(builder: Builder) {
    var `data`: Data? = null
    var device: Device? = null
    var label: Label? = null
    var user: User? = null

    init {
        data = builder.data
        device = builder.device
        label = builder.label
        user = builder.user
    }

    class Builder {
        internal var user: User? = null
        internal var data: Data? = null
        internal var device: Device? = null
        internal var label: Label? = null
        fun user(sex: String, age: Int): Builder {
            user = User(age, sex)
            return this
        }

        fun device(sn: String): Builder {
            device = Device(sn)
            return this
        }

        fun data(source: String): Builder {
            data = Data(source)
            return this
        }

        fun label(mode: String, case: String): Builder {
            label = Label(case, mode)
            return this
        }

        fun build(): StorageSettings {
            return StorageSettings(this)
        }
    }
}