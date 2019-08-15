package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.Service

class EnterAffectiveCloudConfig internal constructor(builder: Builder) {
    var appKey:String? = null
    var appSecret:String? = null
    var userName:String? = null
    var userId:String? = null
    var mAffectiveSubscribeParams: AffectiveSubscribeParams? = null
    var mBiodataSubscribeParams: BiodataSubscribeParams? = null
    var uri: String? = null
    var websocketTimeout: Int? = null
    var availableBiodataServices: List<Service>? = null
    var availableAffectiveServices: List<Service>? = null

    init {
        this.appKey = builder.appKey
        this.appSecret = builder.appSecret
        this.userName = builder.userName
        this.userId = builder.userId
        this.mBiodataSubscribeParams = builder.mBiodataSubscribeParams
        this.mAffectiveSubscribeParams = builder.mAffectiveSubscribeParams
        this.uri = builder.uri
        this.websocketTimeout = builder.websocketTimeout
        this.availableBiodataServices = builder.availableBiodataServices
        this.availableAffectiveServices = builder.availableAffectiveServices
    }

    class Builder(var appKey: String,var appSecret: String, var userName: String, var userId: String) {
        var mAffectiveSubscribeParams: AffectiveSubscribeParams? = null
        var mBiodataSubscribeParams: BiodataSubscribeParams? = null
        var uri: String? = null
        var websocketTimeout: Int? = null
        var availableBiodataServices: List<Service>? = null
        var availableAffectiveServices: List<Service>? = null

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

        fun build(): EnterAffectiveCloudConfig {
            return EnterAffectiveCloudConfig(this)
        }
    }
}