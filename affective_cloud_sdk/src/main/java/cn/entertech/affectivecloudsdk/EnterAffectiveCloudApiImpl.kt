package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService.Companion.UPLOAD_BCG_PACKAGE_COUNT
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService.Companion.UPLOAD_GYRO_PACKAGE_COUNT
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.PhysiologyDataCategory
import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import cn.entertech.affectivecloudsdk.utils.ConvertUtil
import cn.entertech.affectivecloudsdk.utils.MD5Encode
import cn.entertech.affectivecloudsdk.utils.ReportGenerator
import com.google.gson.Gson
import org.java_websocket.handshake.ServerHandshake
import java.util.concurrent.CopyOnWriteArrayList

class EnterAffectiveCloudApiImpl internal constructor(
    var websocketAddress: String,
    var timeout: Int = 10000,
    var appKey: String?,
    var appSecret: String?,
    var userId: String?, var uploadCycle: Int? = DEFAULT_UPLOAD_CYCLE
) : BaseApi {
    private var mSubmitCallback: Callback? = null
    private var mAffectiveReportGenerator: ReportGenerator? = null
    private var mAffectiveReportCallback: Callback2<java.util.HashMap<Any, Any?>>? = null
    private var mBiodataReprotGenerator: ReportGenerator? = null
    private var mBiodataReportCallback: Callback2<java.util.HashMap<Any, Any?>>? = null
    private var mAffectiveDataResponseCallback: Callback2<RealtimeAffectiveData>? = null
    private var mBiodataResponseCallback: Callback2<RealtimeBioData>? = null
    private var mAffectiveStartCallback: Callback? = null
    private var mWebSocketCloseCallback: Callback? = null
    private var mAffectiveFinishCallback: Callback? = null
    private var mAffectiveUnsubscribeCallback: Callback2<SubAffectiveDataFields>? = null
    private var mBiodataUnsubscribeCallback: Callback2<SubBiodataFields>? = null
    private var mAffectiveSubscribeCallback: Callback2<SubAffectiveDataFields>? = null
    private var mBiodataSubscribeCallback: Callback2<SubBiodataFields>? = null
    private var mBiodataInitCallback: Callback? = null
    private var mRestoreCallback: Callback? = null
    private var mCreateSessionCallback: Callback2<String>? = null
    private var mSubscribeAffectiveData: List<Any>? = null
    private var mSubscribeBioData: List<Any>? = null
    private var mStartedAffectiveDataCategories: List<PhysiologyDataCategory>? = null
    private var mSign: String? = null

    /*service type：session、biodata、affective*/
    /*情感云平台服务分类：session（会话）、biodata（基础数据服务）、affective（情感数据服务）*/
    val SERVER_SESSION = "session"
    val SERVER_BIO_DATA = "biodata"
    val SERVER_AFFECTIVE = "affective"
    var mWebSocketHelper: WebSocketHelper? = null
    var mSessionId: String? = null

    private val TAG = "EnterAffectiveCloudApi"

    companion object {
        const val DEFAULT_UPLOAD_EEG_PACKAGE_COUNT = 30
        const val DEFAULT_UPLOAD_HR_PACKAGE_COUNT = 2
        const val DEFAULT_UPLOAD_PEPR_PACKAGE_COUNT = 15
        const val BASE_UPLOAD_EEG_PACKAGE_COUNT = 50
        const val BASE_UPLOAD_HR_PACKAGE_COUNT = 3
        const val UPLOAD_MCEEG_PACKAGE_COUNT = 30
        const val EEG_PACKAGE_LENGTH = 20
        const val HR_PACKAGE_LENGTH = 1
        const val PEPR_PACKAGE_LENGTH = 15
        const val DEFAULT_UPLOAD_CYCLE = 3
    }

    var uploadEEGTriggerCount =
        DEFAULT_UPLOAD_EEG_PACKAGE_COUNT * EEG_PACKAGE_LENGTH * DEFAULT_UPLOAD_CYCLE
    var uploadHRTriggerCount =
        DEFAULT_UPLOAD_HR_PACKAGE_COUNT * HR_PACKAGE_LENGTH * DEFAULT_UPLOAD_CYCLE
    var uploadPEPRTriggerCount =
        DEFAULT_UPLOAD_PEPR_PACKAGE_COUNT * PEPR_PACKAGE_LENGTH * DEFAULT_UPLOAD_CYCLE
    var uploadMCEEGPackageTriggerCount = UPLOAD_MCEEG_PACKAGE_COUNT * DEFAULT_UPLOAD_CYCLE
    var uploadBCGPackageTriggerCount = UPLOAD_BCG_PACKAGE_COUNT * DEFAULT_UPLOAD_CYCLE
    var uploadGyroPackageTriggerCount = UPLOAD_GYRO_PACKAGE_COUNT * DEFAULT_UPLOAD_CYCLE

    constructor(
        websocketAddress: String,
        appKey: String, appSecret: String,
        userId: String, uploadCycle: Int
    ) : this(websocketAddress, 10000, appKey, appSecret, userId, uploadCycle)

    constructor(
        websocketAddress: String,
        timeout: Int = 10000
    ) : this(websocketAddress, timeout, null, null, null)

    init {
        initUploadTrigger()
        mWebSocketHelper = WebSocketHelper(websocketAddress, timeout)
        mWebSocketHelper?.addRawJsonResponseListener {
            AffectiveLogHelper.d(TAG,"RawJsonResponse : $it")
            var response = Gson().fromJson(it, ResponseBody::class.java)
            if (response.isCreateOp()) {
                if (response.code == 0) {
                    mSessionId = response.getSessionId()
                    mCreateSessionCallback?.onSuccess(mSessionId)
                } else {
                    mCreateSessionCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isRestoreOp()) {
                if (response.code == 0) {
                    mRestoreCallback?.onSuccess()
                } else {
                    mRestoreCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isInitBiodataOp()) {
                if (response.code == 0) {
                    mBiodataInitCallback?.onSuccess()
                } else {
                    mBiodataInitCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isSubmitOp()) {
                if (response.code == 0) {
                    mSubmitCallback?.onSuccess()
                } else {
                    mSubmitCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isStartAffectiveOp()) {
                if (response.code == 0) {
                    mAffectiveStartCallback?.onSuccess()
                } else {
                    mAffectiveStartCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isBiodataSubOp()) {
                if (response.code == 0) {
                    mBiodataSubscribeCallback?.onSuccess(response.getBiodataSubFields())
                } else {
                    mBiodataSubscribeCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isAffectiveSubOp()) {
                if (response.code == 0) {
                    mAffectiveSubscribeCallback?.onSuccess(response.getAffectiveSubFields())
                } else {
                    mAffectiveSubscribeCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isBiodataUnsubOp()) {
                if (response.code == 0) {
                    mBiodataUnsubscribeCallback?.onSuccess(response.getBiodataSubFields())
                } else {
                    mBiodataUnsubscribeCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isAffectiveUnsubOp()) {
                if (response.code == 0) {
                    mAffectiveUnsubscribeCallback?.onSuccess(response.getAffectiveSubFields())
                } else {
                    mAffectiveUnsubscribeCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isBiodataResponse()) {
                if (response.code == 0) {
                    mBiodataResponseCallback?.onSuccess(response.getRealtimeBioData())
                } else {
                    mBiodataResponseCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isAffectivedataResponse()) {
                if (response.code == 0) {
                    mAffectiveDataResponseCallback?.onSuccess(response.getRealtimeAffectiveData())
                } else {
                    mAffectiveDataResponseCallback?.onError(Error(response.code, response.msg))
                }
            }
            if (response.isReportBiodata()) {
                if (response.code == 0) {
                    val report = mBiodataReprotGenerator?.appendBioDataResponse(response)
                    if (report != null) {
                        mBiodataReportCallback?.onSuccess(report)
                    }
                } else {
                    mBiodataReportCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isReportAffective()) {
                if (response.code == 0) {
                    val report = mAffectiveReportGenerator?.appendAffectiveDataResponse(response)
                    if (report != null) {
                        mAffectiveReportCallback?.onSuccess(report)
                    }
                } else {
                    mAffectiveReportCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isAffectiveFinish()) {
                if (response.code == 0) {
                    mAffectiveFinishCallback?.onSuccess()
                } else {
                    mAffectiveFinishCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isSessionClose()) {
                if (response.code == 0) {
                    mWebSocketCloseCallback?.onSuccess()
                } else {
                    mWebSocketCloseCallback?.onError(Error(response.code, response.msg))
                }
            }
        }
    }

    fun initUploadTrigger() {
        if (uploadCycle != 0) {
            uploadEEGTriggerCount =
                BASE_UPLOAD_EEG_PACKAGE_COUNT * EEG_PACKAGE_LENGTH * uploadCycle!!
            uploadHRTriggerCount =
                BASE_UPLOAD_HR_PACKAGE_COUNT * HR_PACKAGE_LENGTH * uploadCycle!!
            uploadMCEEGPackageTriggerCount = UPLOAD_MCEEG_PACKAGE_COUNT * uploadCycle!!
            uploadBCGPackageTriggerCount = UPLOAD_BCG_PACKAGE_COUNT * uploadCycle!!
            uploadPEPRTriggerCount =
                DEFAULT_UPLOAD_PEPR_PACKAGE_COUNT * PEPR_PACKAGE_LENGTH * uploadCycle!!
        }
    }

    override fun openWebSocket(webSocketCallback: WebSocketCallback) {
        mWebSocketHelper?.open(webSocketCallback)
    }


    override fun closeWebSocket() {
        mWebSocketHelper?.close()
    }

    override fun closeConnection(code: Int, message: String) {
        mWebSocketHelper?.closeConnection(code, message)
    }


    override fun isWebSocketOpen(): Boolean {
        if (mWebSocketHelper == null) {
            AffectiveLogHelper.e(TAG,"mWebSocketHelper is null")
            return false
        }
        return mWebSocketHelper!!.isOpen()
    }

    override fun createSession(callback2: Callback2<String>) {
        if (appKey == null || appSecret == null || userId == null) {
            throw IllegalArgumentException("constructor invoke error:appKey,appSecret or userId might be null")
        } else {
            this.mCreateSessionCallback = callback2
            var timestamp = "${System.currentTimeMillis()}"
            var userIdEncoded = MD5Encode(userId!!).toUpperCase()
            var md5Params =
                "app_key=$appKey&app_secret=$appSecret&timestamp=$timestamp&user_id=$userIdEncoded"
            mSign = MD5Encode(md5Params).toUpperCase()
            var requestBodyMap = HashMap<Any, Any>()
            requestBodyMap["app_key"] = appKey!!
            requestBodyMap["sign"] = mSign!!
            requestBodyMap["user_id"] = userIdEncoded
            requestBodyMap["timestamp"] = timestamp
            requestBodyMap["upload_cycle"] = uploadCycle!!
            var requestBody = RequestBody(SERVER_SESSION, "create", requestBodyMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
        }
    }

    override fun isSessionCreated(): Boolean {
        return mSessionId != null && mSign != null
    }

    override fun getSessionId(): String? {
        return mSessionId
    }

    private fun sendRestore() {
        if (appKey == null || appSecret == null || userId == null) {
            throw IllegalArgumentException("constructor invoke error:appKey,appSecret or userId might be null")
        } else {
            var userIdEncoded = MD5Encode(userId!!).toUpperCase()
            var timestamp = "${System.currentTimeMillis()}"
            var md5Params =
                "app_key=$appKey&app_secret=$appSecret&timestamp=$timestamp&user_id=$userIdEncoded"
            mSign = MD5Encode(md5Params).toUpperCase()
            var requestBodyMap = HashMap<Any, Any>()
            requestBodyMap["session_id"] = mSessionId!!
            requestBodyMap["app_key"] = appKey!!
            requestBodyMap["sign"] = mSign!!
            requestBodyMap["timestamp"] = timestamp
            requestBodyMap["user_id"] = MD5Encode(userId!!).toUpperCase()
            requestBodyMap["upload_cycle"] = uploadCycle!!
            var requestBody = RequestBody(SERVER_SESSION, "restore", requestBodyMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
        }
    }

    override fun restore(callback: Callback) {
        this.mRestoreCallback = callback
        if (!isSessionCreated()) {
            throw IllegalStateException(
                "session is not exit,restore is not illegal " +
                        ", please create a session directly"
            )
        }
        if (isWebSocketOpen()) {
            sendRestore()
        } else {
            mWebSocketHelper?.open(object : WebSocketCallback {
                override fun onOpen(serverHandshake: ServerHandshake?) {
                    sendRestore()
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    var error = Error(-1, "web socket closed:$reason")
                    callback.onError(error)
                }

                override fun onError(e: Exception?) {
                    var error = Error(-1, e.toString())
                    callback.onError(error)
                }
            })
        }
    }

    override fun initBiodataServices(bioDataCategoryList: List<BioDataCategory>, callback: Callback) {
        initBiodataServices(bioDataCategoryList, callback,null)
    }

    override fun initBiodataServices(
        bioDataCategoryList: List<BioDataCategory>,
        callback: Callback,
        optionParams: HashMap<String, Any?>?
    ) {
        this.mBiodataInitCallback = callback
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = bioDataCategoryList.map { it.value }
        if (optionParams != null) {
            if (optionParams.containsKey("bio_data_tolerance")) {
                if (optionParams["bio_data_tolerance"] != null) {
                    @Suppress("UNCHECKED_CAST")
                    var map = optionParams["bio_data_tolerance"]!! as Map<Any, Any>
                    requestBodyMap["bio_data_tolerance"] = map

                }
            }
            if (optionParams.containsKey("storage_settings")) {
                if (optionParams["storage_settings"] != null) {
                    @Suppress("UNCHECKED_CAST")
                    var map = optionParams["storage_settings"]!! as Map<Any, Any>
                    requestBodyMap["storage_settings"] = map

                }
            }
            if (optionParams.containsKey("algorithm_params")) {
                if (optionParams["algorithm_params"] != null) {
                    @Suppress("UNCHECKED_CAST")
                    var map = optionParams["algorithm_params"]!! as Map<Any, Any>
                    requestBodyMap["algorithm_params"] = map
                }
            }
        }
        var requestBody = RequestBody(SERVER_BIO_DATA, "init", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }


    override fun initAffectiveDataServices(bioOrAffectiveDataCategories: List<PhysiologyDataCategory>, callback: Callback) {
        this.mAffectiveStartCallback = callback
        this.mStartedAffectiveDataCategories = bioOrAffectiveDataCategories
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = bioOrAffectiveDataCategories.map { it.value }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "start", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    var brainDataBuffer = CopyOnWriteArrayList<Int>()
    var lastEEGPackageNum: Int? = null
    override fun appendEEGData(brainData: ByteArray) {
        if (brainData.size == EEG_PACKAGE_LENGTH) {
            val curPackageNum = brainData[0] * 256 + brainData[1]
            if (!(lastEEGPackageNum != null && curPackageNum == lastEEGPackageNum!!)) {
                for (byte in brainData) {
                    var unchart = ConvertUtil.converUnchart(byte)
                    brainDataBuffer.add(unchart)
                    if (brainDataBuffer.size >= uploadEEGTriggerCount) {
                        var dataMap = HashMap<Any, Any>()
                        dataMap["eeg"] = brainDataBuffer.toIntArray()
                        var requestBody =
                            RequestBody(SERVER_BIO_DATA, "upload", dataMap)
                        var requestJson = Gson().toJson(requestBody)
                        mWebSocketHelper?.sendMessage(requestJson)
                        brainDataBuffer.clear()
                    }
                }
            }
            lastEEGPackageNum = curPackageNum
        }
    }

    var brainDataBufferSSVEP = CopyOnWriteArrayList<Int>()
    override fun appendDCEEGData(brainData: ByteArray) {
        for (byte in brainData) {
            var unchart = ConvertUtil.converUnchart(byte)
            brainDataBufferSSVEP.add(unchart)
            if (brainDataBufferSSVEP.size >= uploadEEGTriggerCount) {
                var dataMap = HashMap<Any, Any>()
                dataMap["dceeg-ssvep"] = brainDataBufferSSVEP.toIntArray()
                var requestBody =
                    RequestBody(SERVER_BIO_DATA, "upload", dataMap)
                var requestJson = Gson().toJson(requestBody)
                mWebSocketHelper?.sendMessage(requestJson)
                brainDataBufferSSVEP.clear()
            }
        }
    }

    var mceegDataBuffer = CopyOnWriteArrayList<Int>()
    var mceegPackageCount = 0
    override fun appendMCEEGData(mceegData: ByteArray) {
        mceegDataBuffer.addAll(mceegData.toList().map { ConvertUtil.converUnchart(it) })
        mceegPackageCount++
        if (mceegPackageCount >= uploadMCEEGPackageTriggerCount) {
            var dataMap = HashMap<Any, Any>()
            dataMap["mceeg"] = mceegDataBuffer.toIntArray()
            var requestBody =
                RequestBody(SERVER_BIO_DATA, "upload", dataMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
            mceegPackageCount = 0
            mceegDataBuffer.clear()
        }
    }

    var heartRateDataBuffer = CopyOnWriteArrayList<Int>()
    override fun appendHeartData(heartRateData: Int) {
        heartRateDataBuffer.add(heartRateData)
        if (heartRateDataBuffer.size >= uploadHRTriggerCount) {
            var dataMap = HashMap<Any, Any>()
            dataMap["hr-v2"] = heartRateDataBuffer.toIntArray()
            var requestBody =
                RequestBody(SERVER_BIO_DATA, "upload", dataMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
            heartRateDataBuffer.clear()
        }
    }

    var peprDataBuffer = CopyOnWriteArrayList<Int>()
    var lastPEPRPackageNum:Int? = null
    override fun appendPEPRData(peprData: ByteArray) {
        if (peprData.size == PEPR_PACKAGE_LENGTH) {
            val curPackageNum = peprData[0] * 256 + peprData[1]
            if (!(lastPEPRPackageNum != null && curPackageNum == lastPEPRPackageNum!!)) {
                peprDataBuffer.addAll(peprData.toList().map { ConvertUtil.converUnchart(it) })
                if (peprDataBuffer.size >= uploadPEPRTriggerCount) {
                    var dataMap = HashMap<Any, Any>()
                    dataMap["pepr"] = peprDataBuffer.toIntArray()
                    var requestBody =
                        RequestBody(SERVER_BIO_DATA, "upload", dataMap)
                    var requestJson = Gson().toJson(requestBody)
                    mWebSocketHelper?.sendMessage(requestJson)
                    peprDataBuffer.clear()
                }
            }
            lastPEPRPackageNum = curPackageNum
        }
    }

    var bcgDataBuffer = CopyOnWriteArrayList<Int>()
    var bcgPackageCount = 0
    override fun appendBCGData(bcgData: ByteArray, packageCount: Int) {
        uploadBCGPackageTriggerCount = packageCount * uploadCycle!!
        bcgDataBuffer.addAll(bcgData.toList().map { ConvertUtil.converUnchart(it) })
        bcgPackageCount++
        if (bcgPackageCount >= uploadBCGPackageTriggerCount) {
            var dataMap = HashMap<Any, Any>()
            dataMap["bcg"] = bcgDataBuffer.toIntArray()
            var requestBody =
                RequestBody(SERVER_BIO_DATA, "upload", dataMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
            bcgPackageCount = 0
            bcgDataBuffer.clear()
        }
    }


    var gyroDataBuffer = CopyOnWriteArrayList<Int>()
    var gyroPackageCount = 0
    override fun appendGyroData(gyroData: ByteArray, packageCount: Int) {
        uploadGyroPackageTriggerCount = packageCount * uploadCycle!!
        gyroDataBuffer.addAll(gyroData.toList().map { ConvertUtil.converUnchart(it) })
        gyroPackageCount++
        if (gyroPackageCount >= uploadGyroPackageTriggerCount) {
            var dataMap = HashMap<Any, Any>()
            dataMap["gyro"] = gyroDataBuffer.toIntArray()
            var requestBody =
                RequestBody(SERVER_BIO_DATA, "upload", dataMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
            gyroPackageCount = 0
            gyroDataBuffer.clear()
        }
    }

    override fun subscribeBioData(
        optionalParams: OptionalParamsList,
        response: Callback2<RealtimeBioData>,
        callback: Callback2<SubBiodataFields>
    ) {
        this.mBiodataResponseCallback = response
        this.mBiodataSubscribeCallback = callback
        this.mSubscribeBioData = optionalParams.body()
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "subscribe", null, mSubscribeBioData)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun addBioDataCallback(callback: Callback2<RealtimeBioData>) {
        this.mBiodataResponseCallback = callback
    }

    override fun addAffectiveDataCallback(callback: Callback2<RealtimeAffectiveData>) {
        this.mAffectiveDataResponseCallback = callback
    }

    override fun subscribeAffectiveData(
        optionalParams: OptionalParamsList,
        response: Callback2<RealtimeAffectiveData>,
        callback: Callback2<SubAffectiveDataFields>
    ) {
        this.mAffectiveDataResponseCallback = response
        this.mAffectiveSubscribeCallback = callback
        this.mSubscribeAffectiveData = optionalParams.body()
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "subscribe", null, mSubscribeAffectiveData)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }


    override fun getBiodataReport(
        bioDataCategories: List<BioDataCategory>,
        callback: Callback2<HashMap<Any, Any?>>
    ) {
        this.mBiodataReportCallback = callback
        mBiodataReprotGenerator = ReportGenerator()
        mBiodataReprotGenerator!!.setBioDataCategories(bioDataCategories)
        var requestBodyMap = java.util.HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = bioDataCategories.map { it.value }
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun getAffectivedataReport(
        affectiveDataCategories: List<PhysiologyDataCategory>,
        callback: Callback2<HashMap<Any, Any?>>
    ) {

        this.mAffectiveReportCallback = callback
        this.mAffectiveReportGenerator = ReportGenerator()
        mAffectiveReportGenerator!!.setAffectiveDataCategories(affectiveDataCategories)
        var requestBodyMap = java.util.HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = affectiveDataCategories.map { it.value }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun unsubscribeBioData(
        optionalParams: OptionalParamsList, callback: Callback2<SubBiodataFields>
    ) {
        this.mBiodataUnsubscribeCallback = callback
        if (mSubscribeBioData == null) {
            throw IllegalStateException(
                "there is no biodata services subscribed!!"
            )
        }
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "unsubscribe", null, optionalParams.body())
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun unsubscribeAffectiveData(
        optionalParams: OptionalParamsList,
        callback: Callback2<SubAffectiveDataFields>
    ) {
        this.mAffectiveUnsubscribeCallback = callback
        if (mSubscribeAffectiveData == null) {
            throw IllegalStateException(
                "there is no affective services subscribed!!"
            )
        }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "unsubscribe", null, optionalParams.body())
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun finishAffectiveDataServices(affectiveDataCategories: List<PhysiologyDataCategory>, callback: Callback) {
        this.mAffectiveFinishCallback = callback
        if (mStartedAffectiveDataCategories == null) {
            throw IllegalStateException(
                "there is no affective services started!!"
            )
        }
        affectiveDataCategories.forEach {
            if (!mStartedAffectiveDataCategories!!.contains(it)) {
                throw IllegalStateException(
                    "service '${it.value}' was not started or not exit"
                )
            }
        }
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = affectiveDataCategories.map { it.value }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "finish", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }


    override fun finishAllAffectiveDataServices(callback: Callback) {
        mStartedAffectiveDataCategories?.apply {
            finishAffectiveDataServices(this, callback)

        }?: kotlin.run {
            throw IllegalStateException(
                "there is no affective services started!!"
            )
        }
    }

    override fun destroySessionAndCloseWebSocket(callback: Callback) {
        this.mWebSocketCloseCallback = callback
        val requestBody = RequestBody(SERVER_SESSION, "close", null)
        val requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }


    override fun submit(remark: List<RecData>, callback: Callback) {
        this.mSubmitCallback = callback
        val requestBodyMap = java.util.HashMap<Any, Any>()
        requestBodyMap["rec"] = remark
        val requestBody =
            RequestBody(SERVER_BIO_DATA, "submit", requestBodyMap)
        val requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun addRawJsonRequestListener(listener: (String) -> Unit) {
        mWebSocketHelper?.addRawJsonRequestListener(listener)
    }

    override fun addRawJsonResponseListener(listener: (String) -> Unit) {
        mWebSocketHelper?.addRawJsonResponseListener(listener)
    }

    override fun removeRawJsonRequestListener(listener: (String) -> Unit) {
        mWebSocketHelper?.removeRawJsonRequestListener(listener)
    }

    override fun removeRawJsonResponseListener(listener: (String) -> Unit) {
        mWebSocketHelper?.removeRawJsonResponseListener(listener)
    }

    override fun addConnectListener(listener: () -> Unit) {
        mWebSocketHelper?.addConnectListener(listener)
    }

    override fun addDisconnectListener(listener: (String) -> Unit) {
        mWebSocketHelper?.addDisconnectListener(listener)
    }

    override fun removeConnectListener(listener: () -> Unit) {
        mWebSocketHelper?.removeConnectListener(listener)
    }

    override fun removeDisconnectListener(listener: (String) -> Unit) {
        mWebSocketHelper?.removeDisconnectListener(listener)
    }


}