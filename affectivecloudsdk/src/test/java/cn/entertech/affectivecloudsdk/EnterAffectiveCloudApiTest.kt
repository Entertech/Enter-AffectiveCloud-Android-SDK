package cn.entertech.affectivecloudsdk

import android.util.Log
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.BaseApi
import cn.entertech.affectivecloudsdk.interfaces.Callback
import cn.entertech.affectivecloudsdk.interfaces.Callback2
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.CountDownLatch

import cn.entertech.affectivecloudsdk.utils.FileUtil.readFile
import junit.framework.TestCase.assertEquals
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner::class)
@PrepareForTest(Log::class)
@PowerMockIgnore("jdk.internal.reflect.*","javax.net.ssl.*")
class EnterAffectiveCloudApiTest {

    companion object {
        var APP_KEY = "6eabf68e-760e-11e9-bd82-0242ac140006"
        var APP_SECRET = "68a09cf8e4e06718b037c399f040fb7e"
        /*自己的用户ID：邮箱或者手机号码*/
        var USER_ID = "12809@qq.com"
        var EEG_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_eegdata.txt"
        var HR_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_hrdata.txt"
        var websocketAddress = "wss://server.affectivecloud.com/ws/algorithm/v1/"
        internal var availableAffectiveServices: MutableList<Service> = ArrayList()
        internal var availableBioServices: MutableList<Service> = ArrayList()
        private var biodataSubscribeParams: BiodataSubscribeParams? = null
        private var affectiveSubscribeParams: AffectiveSubscribeParams? = null
        private var rawJsonResponseFunction: Function1<String, Unit>? = null
        private var rawJsonRequestFunction: Function1<String, Unit>? = null
        private var mEnterAffectiveCloudApi: BaseApi? = null



        @BeforeClass
        @JvmStatic
        fun init() {
            PowerMockito.mockStatic(Log::class.java)
            availableAffectiveServices.add(Service.ATTENTION)
            availableAffectiveServices.add(Service.PRESSURE)
            availableAffectiveServices.add(Service.AROUSAL)
            availableAffectiveServices.add(Service.RELAXATION)
            availableAffectiveServices.add(Service.PLEASURE)
            availableBioServices.add(Service.EEG)
            availableBioServices.add(Service.HR)
            biodataSubscribeParams = BiodataSubscribeParams.Builder()
                .requestAllHrData()
                .requestAllEEGData()
                .build()

            affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
                .requestAttention()
                .requestRelaxation()
                .requestPressure()
                .requestPleasure()
                .requestArousal()
                .build()
            mEnterAffectiveCloudApi =
                EnterAffectiveCloudApiFactory.createApi(websocketAddress, APP_KEY, APP_SECRET, USER_ID)

            rawJsonResponseFunction = fun(s: String): Unit {
                println(s)

            }
            rawJsonRequestFunction = fun(s: String): Unit {
                //                System.out.println(s);

            }
            mEnterAffectiveCloudApi!!.addRawJsonResponseListener(rawJsonResponseFunction!!)
            mEnterAffectiveCloudApi!!.addRawJsonRequestListener(rawJsonRequestFunction!!)
        }

        @AfterClass
        @JvmStatic
        fun release() {
            PowerMockito.mockStatic(Log::class.java)
            val results = BooleanArray(2)
            val countDownLatch = CountDownLatch(2)
            mEnterAffectiveCloudApi?.finishAllAffectiveDataServices(object:Callback{
                override fun onSuccess() {
                    results[0] = true
                    countDownLatch.countDown()
                }
                override fun onError(error: Error?) {
                    results[0] = false
                    countDownLatch.countDown()
                }

            })
            mEnterAffectiveCloudApi?.destroySessionAndCloseWebSocket(object : Callback {
                override fun onSuccess() {
                    results[1] = true
                    countDownLatch.countDown()
                }

                override fun onError(error: Error?) {
                    results[1] = false
                    countDownLatch.countDown()
                }
            })
            try {
                countDownLatch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            mEnterAffectiveCloudApi?.removeRawJsonResponseListener(rawJsonResponseFunction!!)
            mEnterAffectiveCloudApi?.removeRawJsonRequestListener(rawJsonRequestFunction!!)
            assertEquals(true, results[0])
            assertEquals(true, results[1])
        }
    }

    @Test
    fun testConnectSocket() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(1)
        val countDownLatch = CountDownLatch(1)
        mEnterAffectiveCloudApi?.openWebSocket(object : WebSocketCallback {
            override fun onOpen(serverHandshake: ServerHandshake?) {
                results[0] = true
                countDownLatch.countDown()
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                results[0] = false
                countDownLatch.countDown()
            }

            override fun onError(e: Exception?) {
                results[0] = false
                countDownLatch.countDown()

            }
        })
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        assertEquals(true, results[0])
    }

    @Test
    fun testCreateSession() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(1)
        val countDownLatch = CountDownLatch(1)
        mEnterAffectiveCloudApi?.createSession(object : Callback2<String> {
            override fun onSuccess(t: String?) {
                results[0] = true
                countDownLatch.countDown()
            }

            override fun onError(error: Error?) {
                results[0] = false
                countDownLatch.countDown()
            }
        })
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        assertEquals(true, results[0])
    }

    @Test
    fun testInitBio() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(1)
        val countDownLatch = CountDownLatch(1)
        mEnterAffectiveCloudApi?.initBiodataServices(availableBioServices, object : Callback {
            override fun onSuccess() {
                results[0] = true
                countDownLatch.countDown()
            }

            override fun onError(error: Error?) {
                results[0] = false
                countDownLatch.countDown()
            }

        })

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        assertEquals(true, results[0])
    }

    @Test
    fun testInitAffective() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(1)
        val countDownLatch = CountDownLatch(1)
        mEnterAffectiveCloudApi?.initAffectiveDataServices(availableAffectiveServices,
            object : Callback {
                override fun onSuccess() {
                    results[0] = true
                    countDownLatch.countDown()
                }

                override fun onError(error: Error?) {
                    results[0] = false
                    countDownLatch.countDown()
                }

            })

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        assertEquals(true, results[0])
    }


    @Test
    fun testSubscribeBiodata() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(1)
        val countDownLatch = CountDownLatch(1)
        mEnterAffectiveCloudApi?.subscribeBioData(biodataSubscribeParams!!, object : Callback2<RealtimeBioData> {
            override fun onSuccess(t: RealtimeBioData?) {

            }

            override fun onError(error: Error?) {
            }

        }, object : Callback2<SubBiodataFields> {
            override fun onSuccess(t: SubBiodataFields?) {
                results[0] = true
                countDownLatch.countDown()
            }

            override fun onError(error: Error?) {
                results[0] = false
                countDownLatch.countDown()
            }

        })

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        assertEquals(true, results[0])
    }

    @Test
    fun testSubscribeAffectiveData() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(1)
        val countDownLatch = CountDownLatch(1)
        mEnterAffectiveCloudApi?.subscribeAffectiveData(affectiveSubscribeParams!!,
            object : Callback2<RealtimeAffectiveData> {
                override fun onSuccess(t: RealtimeAffectiveData?) {
                }

                override fun onError(error: Error?) {
                }

            },
            object : Callback2<SubAffectiveDataFields> {
                override fun onSuccess(t: SubAffectiveDataFields?) {
                    results[0] = true
                    countDownLatch.countDown()
                }

                override fun onError(error: Error?) {
                    results[0] = false
                    countDownLatch.countDown()

                }

            })

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        assertEquals(true, results[0])
    }

    @Test
    fun testUploadRawDataAndReport() {
        PowerMockito.mockStatic(Log::class.java)
        uploadEEGRawData()
        uploadHRRawData()
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val attention = doubleArrayOf(0.0)
        val relaxation = doubleArrayOf(0.0)
        val pleasure = doubleArrayOf(0.0)
        val pressure = doubleArrayOf(0.0)
        val arousal = doubleArrayOf(0.0)
        val hr = doubleArrayOf(0.0)
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(2)
        val countDownLatch = CountDownLatch(2)

        mEnterAffectiveCloudApi!!.getBiodataReport(availableBioServices, object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(objectObjectHashMap: HashMap<Any, Any?>?) {
                val hrMap: Map<Any, Any>
                if (objectObjectHashMap != null && objectObjectHashMap.containsKey("hr")) {
                    hrMap = objectObjectHashMap["hr"] as Map<Any, Any>
                    if (hrMap.containsKey("hr_avg")) {
                        hr[0] = hrMap["hr_avg"] as Double
                    }
                }
                results[0] = true
                countDownLatch.countDown()
            }

            override fun onError(error: Error?) {
                results[0] = false
                countDownLatch.countDown()
            }
        })
        mEnterAffectiveCloudApi?.getAffectivedataReport(availableAffectiveServices,
            object : Callback2<HashMap<Any, Any?>> {
                override fun onSuccess(objectObjectHashMap: HashMap<Any, Any?>?) {
                    val attentionMap: Map<Any, Any>
                    if (objectObjectHashMap != null && objectObjectHashMap.containsKey("attention")) {
                        attentionMap = objectObjectHashMap["attention"] as Map<Any, Any>
                        if (attentionMap.containsKey("attention_avg")) {
                            attention[0] = attentionMap["attention_avg"] as Double
                        }
                    }
                    val relaxationMap: Map<Any, Any>
                    if (objectObjectHashMap != null && objectObjectHashMap.containsKey("relaxation")) {
                        relaxationMap = objectObjectHashMap["relaxation"] as Map<Any, Any>
                        if (relaxationMap.containsKey("relaxation_avg")) {
                            relaxation[0] = relaxationMap["relaxation_avg"] as Double
                        }
                    }
                    val pleasureMap: Map<Any, Any>
                    if (objectObjectHashMap != null && objectObjectHashMap.containsKey("pleasure")) {
                        pleasureMap = objectObjectHashMap["pleasure"] as Map<Any, Any>
                        if (pleasureMap.containsKey("pleasure_avg")) {
                            pleasure[0] = pleasureMap["pleasure_avg"] as Double
                        }
                    }

                    val pressureMap: Map<Any, Any>
                    if (objectObjectHashMap != null && objectObjectHashMap.containsKey("pressure")) {
                        pressureMap = objectObjectHashMap["pressure"] as Map<Any, Any>
                        if (pressureMap.containsKey("pressure_avg")) {
                            pressure[0] = pressureMap["pressure_avg"] as Double
                        }
                    }
                    val arousalMap: Map<Any, Any>
                    if (objectObjectHashMap != null && objectObjectHashMap.containsKey("arousal")) {
                        arousalMap = objectObjectHashMap["arousal"] as Map<Any, Any>
                        if (arousalMap.containsKey("arousal_avg")) {
                            arousal[0] = arousalMap["arousal_avg"] as Double
                        }
                    }
                    results[1] = true
                    countDownLatch.countDown()
                }

                override fun onError(error: Error?) {
                    results[1] = false
                    countDownLatch.countDown()
                }
            })

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        assertEquals(true, results[0])
        assertEquals(true, results[1])
        assertEquals(57.51, attention[0], 5.0)
        assertEquals(49.47, relaxation[0], 5.0)
        assertEquals(41.93, pleasure[0], 5.0)
        assertEquals(35.17, pressure[0], 5.0)
        assertEquals(40.74, arousal[0], 5.0)
        assertEquals(67.0, hr[0], 5.0)
    }

    fun uploadEEGRawData() {
        Thread(Runnable {
            val data = readFile(EEG_TEST_FILE_PATH)
            val eeg = data!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var i = 0
            while (i < eeg.size) {
                val eegs = ByteArray(20)
                for (j in 0..19) {
                    eegs[j] = Integer.parseInt(eeg[j + i]).toByte()
                }
                mEnterAffectiveCloudApi?.appendEEGData(eegs, 600)
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                i = i + 20
            }
        }).start()
    }

    fun uploadHRRawData() {
        val data = readFile(HR_TEST_FILE_PATH)
        val hr = data!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in hr.indices) {
            mEnterAffectiveCloudApi?.appendHeartData(Integer.parseInt(hr[i]), 2)
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

}