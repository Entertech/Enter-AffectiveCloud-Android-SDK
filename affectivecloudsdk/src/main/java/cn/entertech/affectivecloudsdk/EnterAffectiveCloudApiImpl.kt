package cn.entertech.affectivecloudsdk

import android.util.Log
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import cn.entertech.affectivecloudsdk.utils.ConvertUtil
import cn.entertech.affectivecloudsdk.utils.MD5Encode
import cn.entertech.affectivecloudsdk.utils.ReportGenerator
import com.google.gson.Gson
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.HashMap

class EnterAffectiveCloudApiImpl internal constructor(
    var websocketAddress: String,
    var timeout: Int = 10000,
    var appKey: String,
    var appSecret: String,
    var userId: String, var uploadCycle: Int = DEFAULT_UPLOAD_CYCLE
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
    private var mStartedAffectiveServices: List<Service>? = null
    private var mSign: String? = null

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
        const val BASE_UPLOAD_EEG_PACKAGE_COUNT = 50
        const val BASE_UPLOAD_HR_PACKAGE_COUNT = 3
        const val EEG_PACKAGE_LENGTH = 20
        const val HR_PACKAGE_LENGTH = 1
        const val DEFAULT_UPLOAD_CYCLE = 3
    }

    var uploadEEGTriggerCount =
        DEFAULT_UPLOAD_EEG_PACKAGE_COUNT * EEG_PACKAGE_LENGTH * DEFAULT_UPLOAD_CYCLE
    var uploadHRTriggerCount =
        DEFAULT_UPLOAD_HR_PACKAGE_COUNT * HR_PACKAGE_LENGTH * DEFAULT_UPLOAD_CYCLE

    constructor(
        websocketAddress: String,
        appKey: String, appSecret: String,
        userId: String, uploadCycle: Int
    ) : this(websocketAddress, 10000, appKey, appSecret, userId,uploadCycle)

    init {
        initUploadTrigger()
        mWebSocketHelper = WebSocketHelper(websocketAddress, timeout)
        mWebSocketHelper?.addRawJsonResponseListener {
            Log.d(TAG, "receive msg from web socket:$it")
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
                    var report = mBiodataReprotGenerator?.appendResponse(response)
                    if (report != null) {
                        mBiodataReportCallback?.onSuccess(report)
                    }
                } else {
                    mBiodataReportCallback?.onError(Error(response.code, response.msg))
                }
            }

            if (response.isReportAffective()) {
                if (response.code == 0) {
                    var report = mAffectiveReportGenerator?.appendResponse(response)
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
        if (uploadCycle != 0){
            uploadEEGTriggerCount =
                BASE_UPLOAD_EEG_PACKAGE_COUNT * EEG_PACKAGE_LENGTH * uploadCycle
            uploadHRTriggerCount =
                BASE_UPLOAD_HR_PACKAGE_COUNT * HR_PACKAGE_LENGTH * uploadCycle
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
            return false
        }
        return mWebSocketHelper!!.isOpen()
    }

    override fun createSession(callback2: Callback2<String>) {
        this.mCreateSessionCallback = callback2
        var timestamp = "${System.currentTimeMillis()}"
        var userIdEncoded = MD5Encode(userId).toUpperCase()
        var md5Params =
            "app_key=$appKey&app_secret=$appSecret&timestamp=$timestamp&user_id=$userIdEncoded"
        mSign = MD5Encode(md5Params).toUpperCase()
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["app_key"] = appKey
        requestBodyMap["sign"] = mSign!!
        requestBodyMap["user_id"] = userIdEncoded
        requestBodyMap["timestamp"] = timestamp
        requestBodyMap["upload_cycle"] = uploadCycle
        var requestBody = RequestBody(SERVER_SESSION, "create", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun isSessionCreated(): Boolean {
        return mSessionId != null && mSign != null
    }

    override fun getSessionId(): String? {
        return mSessionId
    }

    private fun sendRestore() {
        var userIdEncoded = MD5Encode(userId).toUpperCase()
        var timestamp = "${System.currentTimeMillis()}"
        var md5Params =
            "app_key=$appKey&app_secret=$appSecret&timestamp=$timestamp&user_id=$userIdEncoded"
        mSign = MD5Encode(md5Params).toUpperCase()
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["session_id"] = mSessionId!!
        requestBodyMap["app_key"] = appKey
        requestBodyMap["sign"] = mSign!!
        requestBodyMap["timestamp"] = timestamp
        requestBodyMap["user_id"] = MD5Encode(userId).toUpperCase()
        requestBodyMap["upload_cycle"] = uploadCycle
        var requestBody = RequestBody(SERVER_SESSION, "restore", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
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

    override fun initBiodataServices(serviceList: List<Service>, callback: Callback) {
        this.mBiodataInitCallback = callback
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = serviceList.map { it.value }
        var requestBody = RequestBody(SERVER_BIO_DATA, "init", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun initBiodataServices(
        serviceList: List<Service>,
        callback: Callback,
        optionParams: HashMap<String, Any?>?
    ) {
        this.mBiodataInitCallback = callback
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = serviceList.map { it.value }
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


    override fun initAffectiveDataServices(services: List<Service>, callback: Callback) {
        this.mAffectiveStartCallback = callback
        this.mStartedAffectiveServices = services
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = services.map { it.value }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "start", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    var brainDataBuffer = CopyOnWriteArrayList<Int>()
    override fun appendEEGData(brainData: ByteArray) {
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

    var heartRateDataBuffer = CopyOnWriteArrayList<Int>()
    override fun appendHeartData(heartRateData: Int) {
        heartRateDataBuffer.add(heartRateData)
        if (heartRateDataBuffer.size >= uploadHRTriggerCount) {
            var dataMap = HashMap<Any, Any>()
            dataMap["hr"] = heartRateDataBuffer.toIntArray()
            var requestBody =
                RequestBody(SERVER_BIO_DATA, "upload", dataMap)
            var requestJson = Gson().toJson(requestBody)
            mWebSocketHelper?.sendMessage(requestJson)
            heartRateDataBuffer.clear()
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
        services: List<Service>,
        callback: Callback2<HashMap<Any, Any?>>
    ) {
        this.mBiodataReportCallback = callback
        mBiodataReprotGenerator = ReportGenerator()
        mBiodataReprotGenerator!!.init(services)
        var requestBodyMap = java.util.HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = services.map { it.value }
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }

    override fun getAffectivedataReport(
        services: List<Service>,
        callback: Callback2<HashMap<Any, Any?>>
    ) {
        this.mAffectiveReportCallback = callback
        this.mAffectiveReportGenerator = ReportGenerator()
        mAffectiveReportGenerator!!.init(services)
        var requestBodyMap = java.util.HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = services.map { it.value }
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

    override fun finishAffectiveDataServices(services: List<Service>, callback: Callback) {
        this.mAffectiveFinishCallback = callback
        if (mStartedAffectiveServices == null) {
            throw IllegalStateException(
                "there is no affective services started!!"
            )
        }
        services.forEach {
            if (!mStartedAffectiveServices!!.contains(it)) {
                throw IllegalStateException(
                    "service '${it.value}' was not started or not exit"
                )
            }
        }
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = services.map { it.value }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "finish", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }


    override fun finishAllAffectiveDataServices(callback: Callback) {
        if (mStartedAffectiveServices == null) {
            throw IllegalStateException(
                "there is no affective services started!!"
            )
        }
        finishAffectiveDataServices(mStartedAffectiveServices!!, callback)
    }

    override fun destroySessionAndCloseWebSocket(callback: Callback) {
        this.mWebSocketCloseCallback = callback
        var requestBody = RequestBody(SERVER_SESSION, "close", null)
        var requestJson = Gson().toJson(requestBody)
        mWebSocketHelper?.sendMessage(requestJson)
    }


    override fun submit(remark: List<RecData>, callback: Callback) {
        this.mSubmitCallback = callback
        var requestBodyMap = java.util.HashMap<Any, Any>()
        requestBodyMap["rec"] = remark
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "submit", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
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