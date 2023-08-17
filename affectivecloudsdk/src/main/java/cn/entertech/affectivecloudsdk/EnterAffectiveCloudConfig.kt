package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Service
import cn.entertech.affectivecloudsdk.EnterAffectiveCloudApiImpl.Companion.DEFAULT_UPLOAD_CYCLE
import java.lang.IllegalStateException

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
    var algorithmParams: AlgorithmParams? = null
    var biodataTolerance: BiodataTolerance? = null
    var uploadCycle = DEFAULT_UPLOAD_CYCLE

    companion object {
        private const val AFFECTIVE_CLOUD_ADDRESS = "wss://server.affectivecloud.cn/ws/algorithm/v2/"

        fun proxyInstance(proxy: EnterAffectiveConfigProxy): EnterAffectiveCloudConfig {
            val availableAffectiveServices = proxy.availableAffectiveServices
            val availableBiodataServices = proxy.availableBiodataServices
            val biodataSubscribeParamsBuilder = BiodataSubscribeParams.Builder()
            val affectiveSubscribeParamsBuilder = AffectiveSubscribeParams.Builder()
            val storageSettingsBuild = StorageSettings.Builder()
            availableAffectiveServices.forEach {
                when (it) {
                    Service.ATTENTION -> {
                        affectiveSubscribeParamsBuilder.requestAttention()
                    }

                    Service.PRESSURE -> {
                        affectiveSubscribeParamsBuilder.requestPressure()
                    }

                    Service.RELAXATION -> {
                        affectiveSubscribeParamsBuilder.requestRelaxation()
                    }

                    Service.PLEASURE -> {
                        affectiveSubscribeParamsBuilder.requestPleasure()
                    }

                    Service.COHERENCE -> {
                        affectiveSubscribeParamsBuilder.requestCoherence()
                    }

                    Service.FLOW -> {
                        affectiveSubscribeParamsBuilder.requestFlow()
                    }

                    else -> {

                    }
                }
            }
            availableBiodataServices.forEach {
                when (it) {
                    Service.EEG -> {
                        biodataSubscribeParamsBuilder.requestEEG()
                    }

                    Service.PEPR -> {
                        biodataSubscribeParamsBuilder.requestPEPR()
                    }

                    Service.HR -> {
                        biodataSubscribeParamsBuilder.requestHR()
                    }

                    else -> {
                    }
                }
            }
            var sexType = when (proxy.sex) {
                "m" -> {
                    StorageSettings.Sex.MALE
                }

                "f" -> {
                    StorageSettings.Sex.FEMALE
                }

                else -> {
                    StorageSettings.Sex.OTHER
                }
            }
            storageSettingsBuild.sex(sexType).age(proxy.age)
            val algorithmParamsEEG =
                AlgorithmParamsEEG.Builder()
                    .filterMode(AlgorithmParams.FilterMode.HARD)
                    .powerMode(AlgorithmParams.PowerMode.DB)
                    .build()
            val algorithmParams = AlgorithmParams.Builder().eeg(algorithmParamsEEG).build()
            return Builder(
                proxy.appKey,
                proxy.appSecret,
                proxy.userId
            )
                .url(AFFECTIVE_CLOUD_ADDRESS)
                .timeout(10000)
                .availableBiodataServices(availableBiodataServices)
                .availableAffectiveServices(availableAffectiveServices)
                .biodataSubscribeParams(biodataSubscribeParamsBuilder.build())
                .affectiveSubscribeParams(affectiveSubscribeParamsBuilder.build())
                .biodataTolerance(BiodataTolerance.Builder().eeg(2).build())
                .algorithmParams(algorithmParams)
                .storageSettings(
                    storageSettingsBuild.case(listOf(2))
                        .mode(listOf(3)).build()
                )
                .uploadCycle(1)
                .build()
        }
    }

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
        this.algorithmParams = builder.algorithmParams
        this.biodataTolerance = builder.biodataTolerance
        this.uploadCycle = builder.uploadCycle
    }

    class Builder(var appKey: String, var appSecret: String, var userId: String) {
        var algorithmParams: AlgorithmParams? = null
        var storageSettings: StorageSettings? = null
        var mAffectiveSubscribeParams: AffectiveSubscribeParams? = null
        var mBiodataSubscribeParams: BiodataSubscribeParams? = null
        var uri: String? = null
        var websocketTimeout: Int? = null
        var availableBiodataServices: List<Service>? = null
        var availableAffectiveServices: List<Service>? = null
        var biodataTolerance: BiodataTolerance? = null
        var uploadCycle = DEFAULT_UPLOAD_CYCLE
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

        fun algorithmParams(algorithmParams: AlgorithmParams): Builder {
            this.algorithmParams = algorithmParams
            return this
        }

        fun uploadCycle(uploadCycle: Int): Builder {
            if (uploadCycle < 0 || uploadCycle > 100) {
                throw IllegalStateException("upload cycle must between 0 and 100,include 0 and 100.")
            }
            this.uploadCycle = uploadCycle
            return this
        }

        fun build(): EnterAffectiveCloudConfig {
            return EnterAffectiveCloudConfig(this)
        }
    }

    override fun toString(): String {
        return "EnterAffectiveCloudConfig(appKey=$appKey, appSecret=$appSecret, userId=$userId, mAffectiveSubscribeParams=$mAffectiveSubscribeParams, mBiodataSubscribeParams=$mBiodataSubscribeParams, uri=$uri, websocketTimeout=$websocketTimeout, availableBiodataServices=$availableBiodataServices, availableAffectiveServices=$availableAffectiveServices, storageSettings=$storageSettings, algorithmParams=$algorithmParams, biodataTolerance=$biodataTolerance, uploadCycle=$uploadCycle)"
    }

}