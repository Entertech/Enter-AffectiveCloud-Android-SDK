package cn.entertech.affectivecloudsdk.interfaces

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

    fun closeConnection(code:Int, message:String)

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
     * @param serviceList List<String>
     * @param callback Callback
     */
    fun initBiodataServices(serviceList: List<Service>, callback: Callback)

    /**
     * start affective services before use.
     *
     * @param serviceList List<String>
     * @param callback Callback
     */
    fun initAffectiveDataServices(serviceList: List<Service>, callback: Callback)

    /**
     * Send brain data to affective cloud platform.
     * @param brainData ByteArray
     */
    fun appendEEGData(brainData: ByteArray, triggerCount: Int = 600)

    /**
     * Send heart rate data to affective cloud platform
     * @param heartRateData ByteArray
     */
    fun appendHeartData(heartRateData: Int, triggerCount: Int = 2)

    /**
     * Subscribe biodata. After subscribing, can receive real time analysed biodata from affective cloud platform.
     * @param data HashMap<Any, Any>
     * @param response Callback2<RealtimeBioData>
     * @param callback Callback2<SubBiodataFields>
     */
    fun subscribeBioData(
        subscribeParams: SubscribeParams,
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
        subscribeParams: SubscribeParams,
        response: Callback2<RealtimeAffectiveData>,
        callback: Callback2<SubAffectiveDataFields>
    )

    /**
     * Report biodata.
     * @param services List<String>
     * @param callback Callback2<HashMap<Any, Any?>>
     */
    fun getBiodataReport(services: List<Service>, callback: Callback2<HashMap<Any, Any?>>)

    /**
     * Report affecitve data
     * @param services List<String>
     * @param callback Callback2<HashMap<Any, Any?>>
     */
    fun getAffectivedataReport(services: List<Service>, callback: Callback2<HashMap<Any, Any?>>)

    /**
     * Unsubscribe biodata.
     * @param data HashMap<Any, Any>
     * @param callback Callback2<SubBiodataFields>
     */
    fun unsubscribeBioData(
        subscribeParams: SubscribeParams, callback: Callback2<SubBiodataFields>
    )

    /**
     * Unsubscribe affective data
     * @param data HashMap<Any, Any>
     * @param callback Callback2<SubAffectiveDataFields>
     */
    fun unsubscribeAffectiveData(
        subscribeParams: SubscribeParams, callback: Callback2<SubAffectiveDataFields>
    )

    /**
     * Warning!:Don't forget to call this method to finish affective services after you have used it.
     * @param serviceList List<String>
     * @param callback Callback
     */
    fun finishAffectiveDataServices(serviceList: List<Service>, callback: Callback)

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
    fun addDisconnectListener(listener: () -> Unit)

    /**
     * Remove web socket connect listener
     * @param listener Function0<Unit>
     */
    fun removeConnectListener(listener: () -> Unit)

    /**
     * Remove web socket disconnect listener
     * @param listener Function0<Unit>
     */
    fun removeDisconnectListener(listener: () -> Unit)

}