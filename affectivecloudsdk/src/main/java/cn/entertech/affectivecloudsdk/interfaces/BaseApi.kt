package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affectivecloudsdk.entity.*
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception

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

    /**
     * After web socket connected, should call this method to create a session
     * @param success Callback2<String>
     * @param error Callback
     */
    fun createSession(callback2: Callback2<String, Error>)

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
     * @param success Callback
     * @param error Callback
     */
    fun restore(callback: Callback)

    /**
     * Init biodata services.
     * There are two type services include basic biodata service and affective service, every services
     * should be init or start (biodata service need init ,and affective service is start)before use.
     * Affective services is base of the biodata services,so this method must be
     * called after a sessino create.
     * @param serviceList List<String>
     * @param success Callback
     * @param error Callback
     */
    fun initBiodataServices(serviceList: List<String>, callback: Callback)

    /**
     * start affective services before use
     * @param serviceList List<String>
     * @param success Callback
     * @param error Callback
     */
    fun startAffectiveServices(serviceList: List<String>, callback: Callback)

    /**
     * Send brain data to affective cloud platform.
     * @param brainData ByteArray
     */
    fun appendBrainData(brainData: ByteArray, triggerCount: Int = 600)

    /**
     * Send heart rate data to affective cloud platform
     * @param heartRateData ByteArray
     */
    fun appendHeartData(heartRateData: Int, triggerCount: Int = 2)

    /**
     * Subscribe biodata. After subscribing, can receive real time analysed biodata from affective cloud platform.
     * @param dataTypeList List<String>
     * @param response Callback3<RealtimeBioData>
     * @param success Callback
     * @param error Callback
     */
    fun subscribeBioData(
        data: HashMap<Any, Any>,
        response: Callback2<RealtimeBioData, Error>,
        callback: Callback2<SubBiodataFields, Error>
    )

    /**
     * Subscribe affective data, which is similar to subscribeBioData method.
     *
     * @param dataTypeList List<String>
     * @param response Callback3<RealtimeAffectiveData>
     * @param success Callback
     * @param error Callback
     */
    fun subscribeAffectiveData(
        data: HashMap<Any, Any>,
        response: Callback2<RealtimeAffectiveData, Error>,
        callback: Callback2<SubAffectiveDataFields, Error>
    )

    /**
     * Report biodata
     * @param services List<String>
     * @param callback Callback2<HashMap<Any, Any?>, Error>
     */
    fun reportBiodata(services: List<String>, callback: Callback2<HashMap<Any, Any?>, Error>)

    /**
     * Report affecitve data
     * @param services List<String>
     * @param callback Callback2<HashMap<Any, Any?>, Error>
     */
    fun reportAffective(services: List<String>, callback: Callback2<HashMap<Any, Any?>, Error>)

    /**
     * Unsubscribe biodata
     * @param dataTypeList List<String>
     * @param success Callback
     * @param error Callback
     */
    fun unsubscribeBioData(dataTypeList: List<String>, callback: Callback2<SubBiodataFields, Error>)

    /**
     * Unsubscribe affective data
     * @param dataTypeList List<String>
     * @param success Callback
     * @param error Callback
     */
    fun unsubscribeAffectiveData(dataTypeList: List<String>, callback: Callback2<SubAffectiveDataFields, Error>)

    /**
     * Warning!:Don't forget to call this method to finish affective services after you have used it.
     * @param success Callback
     * @param error Callback
     */
    fun finishAffectiveServices(callback: Callback)

    /**
     * Warning!:Don't forget to call this method to close session if you want to disconnected from platform.
     * @param success Callback
     * @param error Callback
     */
    fun closeWebSocketAndDestroySession(callback: Callback)

}