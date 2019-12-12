package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.Service

class EnterAffectiveCloudConfig internal constructor(builder: Builder) {
    var appKey: String? = null
    var appSecret: String? = null
    var userId: String? = null
    var mAffectiveSubscribeParams: AffectiveSubscribeParams? = null
    var mBiodataSubscribeParams: BiodataSubscribeParams? = null
    var uri: String? = null
    var websocketTimeout: Int? = null
    var availableBiodataServices: List<Service>? = null
    var availableAffectiveServices: List<Service>? = null
    var storageSettings: StorageSettings? = null
    var biodataTolerance: BiodataTolerance? = null

    init {
        this.appKey = builder.appKey
        this.appSecret = builder.appSecret
        this.userId = builder.userId
        this.mBiodataSubscribeParams = builder.mBiodataSubscribeParams
        this.mAffectiveSubscribeParams = builder.mAffectiveSubscribeParams
        this.uri = builder.uri
        this.websocketTimeout = builder.websocketTimeout
        this.availableBiodataServices = builder.availableBiodataServices
        this.availableAffectiveServices = builder.availableAffectiveServices
        this.storageSettings = builder.storageSettings
        this.biodataTolerance = builder.biodataTolerance
    }

    class Builder(var appKey: String, var appSecret: String, var userId: String) {
        var storageSettings: StorageSettings? = null
        var mAffectiveSubscribeParams: AffectiveSubscribeParams? = null
        var mBiodataSubscribeParams: BiodataSubscribeParams? = null
        var uri: String? = null
        var websocketTimeout: Int? = null
        var availableBiodataServices: List<Service>? = null
        var availableAffectiveServices: List<Service>? = null
        var biodataTolerance: BiodataTolerance? = null
        fun url(url: String): Builder {
            uri = url
            return this
        }

        fun timeout(timeout: Int): Builder {
            websocketTimeout = timeout
            return this
        }

        fun availableBiodataServices(services: List<Service>): Builder {
            this.availableBiodataServices = services
            return this
        }

        fun storageSettings(storageSettings: StorageSettings): Builder {
            this.storageSettings = storageSettings
            return this
        }


        fun availableAffectiveServices(services: List<Service>): Builder {
            this.availableAffectiveServices = services
            return this
        }

        fun biodataSubscribeParams(biodataSubscribeParams: BiodataSubscribeParams): Builder {
            this.mBiodataSubscribeParams = biodataSubscribeParams
            return this
        }

        fun affectiveSubscribeParams(affectiveSubscribeParams: AffectiveSubscribeParams): Builder {
            this.mAffectiveSubscribeParams = affectiveSubscribeParams
            return this
        }

        fun biodataTolerance(biodataTolerance: BiodataTolerance): Builder {
            this.biodataTolerance = biodataTolerance
            return this
        }

        fun build(): EnterAffectiveCloudConfig {
            return EnterAffectiveCloudConfig(this)
        }
    }
}