package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.PhysiologyDataCategory
import cn.entertech.affectivecloudsdk.entity.*

/**
 * Entertech
 */
interface BaseApi {
    /**
     * Open web socket between client and cloud platform
     * @param webSocketCallback WebSocketCallback
     */
    fun openWebSocket(webSocketCallback: WebSocketCallback)

    /**
     * Return the status of web socket
     * @return Boolean
     */
    fun isWebSocketOpen(): Boolean

    fun closeWebSocket()

    fun closeConnection(code: Int, message: String)

    /**
     * After web socket connected, should call this method to create a session
     * @param callback2 Callback2<String, Error>
     */
    fun createSession(callback2: Callback2<String>)

    /**
     * Return if session is created
     * @return Boolean
     */
    fun isSessionCreated(): Boolean

    /**
     * Every session created would return a session id ,which is identity for session restore
     * @return String
     */
    fun getSessionId(): String?

    /**
     * Restore the session. If web socket disconnected unexpected,you can
     * call this method to restore the session. Timeout is 10 minutes.
     * @param callback Callback
     */
    fun restore(callback: Callback)

    /**
     * Init biodata services.
     *
     * There are two type services include basic biodata service and affective service, every services
     * should be init or start (biodata service need init ,and affective service is start)before use.
     * Affective services is base of the biodata services,so this method must be
     * called after a sessino create.
     * @param bioDataCategoryList List<String>
     * @param callback Callback
     */
    @Deprecated("")
    fun initBiodataServices(bioDataCategoryList: List<BioDataCategory>, callback: Callback)


    /**
     * Init biodata services with option params
     *
     * @param bioDataCategoryList List<Service>
     * @param callback Callback
     * @param optionParams HashMap<String, Any>
     */
    fun initBiodataServices(
        bioDataCategoryList: List<BioDataCategory>,
        callback: Callback,
        optionParams: java.util.HashMap<String, Any?>?
    )

    /**
     * start affective services before use.
     *
     * @param bioOrAffectiveDataCategories List<String>
     * @param callback Callback
     */
    fun initAffectiveDataServices(bioOrAffectiveDataCategories: List<PhysiologyDataCategory>, callback: Callback)

    /**
     * Send brain data to affective cloud platform.
     * @param brainData ByteArray
     */
    fun appendEEGData(brainData: ByteArray)

    /**
     * Send brain data to affective cloud platform for ssvep
     * @param brainData ByteArray
     */
    fun appendDCEEGData(brainData: ByteArray)

    /**
     * Send heart rate data to affective cloud platform
     * @param heartRateData ByteArray
     */
    fun appendHeartData(heartRateData: Int)

    /**
     * Send multi-channel brain data to affective cloud platform.
     * @param brainData ByteArray
     */
    fun appendMCEEGData(brainData: ByteArray)

    /**
     * ApSendpend pepr data to affective cloud platform.
     *
     * @param peprData
     */
    fun appendPEPRData(peprData: ByteArray)

    /**
     * Send bcg data to affective cloud platform
     *
     * @param bcgData
     */
    fun appendBCGData(bcgData: ByteArray, packageCount: Int)

    /**
     * Append gyro data
     *
     * @param gyroData
     */
    fun appendGyroData(gyroData: ByteArray, packageCount: Int)

    /**
     * Subscribe biodata. After subscribing, can receive real time analysed biodata from affective cloud platform.
     * @param data HashMap<Any, Any>
     * @param response Callback2<RealtimeBioData>
     * @param callback Callback2<SubBiodataFields>
     */
    fun subscribeBioData(
        optionalParams: OptionalParamsList,
        response: Callback2<RealtimeBioData>,
        callback: Callback2<SubBiodataFields>
    )

    /**
     * Subscribe affective data, which is similar to subscribeBioData method.
     * @param data HashMap<Any, Any>
     * @param response Callback2<RealtimeAffectiveData>
     * @param callback Callback2<SubAffectiveDataFields>
     */
    fun subscribeAffectiveData(
        optionalParams: OptionalParamsList,
        response: Callback2<RealtimeAffectiveData>,
        callback: Callback2<SubAffectiveDataFields>
    )

    /**
     * Report biodata.
     * @param bioDataCategories List<String>
     * @param callback Callback2<HashMap<Any, Any?>>
     */
    fun getBiodataReport(bioDataCategories: List<BioDataCategory>, callback: Callback2<HashMap<Any, Any?>>)

    /**
     * Report affecitve data
     * @param affectiveDataCategories List<String>
     * @param callback Callback2<HashMap<Any, Any?>>
     */
    fun getAffectivedataReport(affectiveDataCategories: List<PhysiologyDataCategory>, callback: Callback2<HashMap<Any, Any?>>)

    /**
     * Unsubscribe biodata.
     * @param data HashMap<Any, Any>
     * @param callback Callback2<SubBiodataFields>
     */
    fun unsubscribeBioData(
        optionalParams: OptionalParamsList, callback: Callback2<SubBiodataFields>
    )

    /**
     * Unsubscribe affective data
     * @param data HashMap<Any, Any>
     * @param callback Callback2<SubAffectiveDataFields>
     */
    fun unsubscribeAffectiveData(
        optionalParams: OptionalParamsList, callback: Callback2<SubAffectiveDataFields>
    )

    /**
     * Warning!:Don't forget to call this method to finish affective services after you have used it.
     * @param affectiveDataCategories List<String>
     * @param callback Callback
     */
    fun finishAffectiveDataServices(affectiveDataCategories: List<PhysiologyDataCategory>, callback: Callback)

    /**
     * Finish all affective services started before.
     * @param callback Callback
     */
    fun finishAllAffectiveDataServices(callback: Callback)

    /**
     * Warning!:Don't forget to call this method to close session if you want to disconnected from platform.
     * @param callback Callback
     */
    fun destroySessionAndCloseWebSocket(callback: Callback)

    /**
     * submit remark for this session
     * @param remark RecData
     * @param callback Callback
     */
    fun submit(remark: List<RecData>, callback: Callback)

    /**
     * Add raw json request listener.
     *
     * @param listener Function1<String, Unit>
     */
    fun addRawJsonRequestListener(listener: ((String) -> (Unit)))

    /**
     * Add raw json response listener
     * @param listener Function1<String, Unit>
     */
    fun addRawJsonResponseListener(listener: ((String) -> (Unit)))


    /**
     * Remove raw json request listener.
     *
     * @param listener Function1<String, Unit>
     */
    fun removeRawJsonRequestListener(listener: ((String) -> (Unit)))

    /**
     * Remove raw json response listener
     * @param listener Function1<String, Unit>
     */
    fun removeRawJsonResponseListener(listener: ((String) -> (Unit)))

    /**
     * Add web socket connect listener
     * @param listener Function0<Unit>
     */
    fun addConnectListener(listener: () -> Unit)

    /**
     * Add web socket disconnect listener
     * @param listener Function0<Unit>
     */
    fun addDisconnectListener(listener: (String) -> Unit)

    /**
     * Remove web socket connect listener
     * @param listener Function0<Unit>
     */
    fun removeConnectListener(listener: () -> Unit)

    /**
     * Remove web socket disconnect listener
     * @param listener Function0<Unit>
     */
    fun removeDisconnectListener(listener: (String) -> Unit)

    fun addBioDataCallback(callback: Callback2<RealtimeBioData>)

    fun addAffectiveDataCallback(callback: Callback2<RealtimeAffectiveData>)

}