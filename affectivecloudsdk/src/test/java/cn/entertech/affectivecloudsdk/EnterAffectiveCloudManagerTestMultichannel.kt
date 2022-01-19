package cn.entertech.affectivecloudsdk

import android.util.Log
import cn.entertech.affectivecloudsdk.entity.Error
import cn.entertech.affectivecloudsdk.entity.Service
import cn.entertech.affectivecloudsdk.interfaces.Callback
import cn.entertech.affectivecloudsdk.interfaces.Callback2
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

import java.util.concurrent.CountDownLatch

import cn.entertech.affectivecloudsdk.utils.FileUtil.readFile
import junit.framework.TestCase.assertEquals
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner::class)
@PrepareForTest(Log::class)
@PowerMockIgnore("jdk.internal.reflect.*", "javax.net.ssl.*")
class EnterAffectiveCloudManagerTestMultichannel {

    @Test
    fun testRealtimeData() {
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(2)
        val countDownLatch = CountDownLatch(2)

        enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            results[0] = true
            countDownLatch.countDown()
            null
        }

        enterAffectiveCloudManager!!.addAffectiveDataRealtimeListener {
            results[1] = true
            countDownLatch.countDown()
            null
        }
        uploadPEPRRawData()
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        assertEquals(true, results[0])
        assertEquals(true, results[1])
    }

    @Test
    fun testReportData() {
        val attention = doubleArrayOf(0.0)
        val relaxation = doubleArrayOf(0.0)
        val pleasure = doubleArrayOf(0.0)
        val pressure = doubleArrayOf(0.0)
        val arousal = doubleArrayOf(0.0)
        val hr = doubleArrayOf(0.0)
        PowerMockito.mockStatic(Log::class.java)
        val results = BooleanArray(2)
        val countDownLatch = CountDownLatch(2)

        enterAffectiveCloudManager!!.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
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
        enterAffectiveCloudManager!!.getAffectiveDataReport(object : Callback2<HashMap<Any, Any?>> {
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
        assertEquals(68.36, attention[0], 30.0)
        assertEquals(49.47, relaxation[0], 30.0)
        assertEquals(41.93, pleasure[0], 30.0)
        assertEquals(35.17, pressure[0], 30.0)
        assertEquals(40.74, arousal[0], 30.0)
        assertEquals(67.0, hr[0], 30.0)
    }

    fun uploadMCEEGRawData() {
        Thread(Runnable {
            val data = readFile(MCEEG_TEST_FILE_PATH)
            val mceeg = data!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var i = 0
            while (i < mceeg.size) {
                val eegs = ByteArray(124)
                for (j in 0..123) {
                    eegs[j] = Integer.parseInt(mceeg[j + i]).toByte()
                }
                enterAffectiveCloudManager!!.appendMCEEGData(eegs)
                try {
                    Thread.sleep(20)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                i = i + 124
            }
        }).start()
    }

    fun uploadBCGRawData() {
        val data = readFile(BCG_TEST_FILE_PATH)
        val bcg = data!!.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var i = 0
        while (i < bcg.size) {
            val bcgs = ByteArray(13)
            for (j in 0..12) {
                bcgs[j] = Integer.parseInt(bcg[j + i]).toByte()
            }
            enterAffectiveCloudManager!!.appendBCGData(bcgs)
            try {
                Thread.sleep(40)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            i = i + 13
        }
    }
    fun uploadPEPRRawData() {
        val data = readFile(PEPR_TEST_FILE_PATH)
        val bcg = data!!.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var i = 0
        while (i < bcg.size) {
            val peprs = ByteArray(15)
            for (j in 0..14) {
                peprs[j] = Integer.parseInt(bcg[j + i]).toByte()
            }
            enterAffectiveCloudManager!!.appendPEPRData(peprs)
            try {
                Thread.sleep(40)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            i = i + 15
        }
    }

    companion object {

        var APP_KEY = "015b7118-b81e-11e9-9ea1-8c8590cb54f9"
        var APP_SECRET = "cd9c757ae9a7b7e1cff01ee1bb4d4f98"

        /*自己的用户ID：邮箱或者手机号码*/
        var USER_ID = "12809@qq.com"
        var EEG_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_eegdata.txt"
        var HR_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_hrdata.txt"
        var MCEEG_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/entertech_vr_mceeg.txt"
        var BCG_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/entertech_vr_bcg.txt"
        var PEPR_TEST_FILE_PATH =
            "/Users/daiwanli/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/cushion_pepr.txt"

        var websocketAddress = "wss://server-test.affectivecloud.cn/ws/algorithm/v2/"

        //        var websocketAddress = "wss://server.affectivecloud.com/ws/algorithm/v1/"
        internal var availableAffectiveServices: MutableList<Service> = ArrayList()
        internal var availableBioServices: MutableList<Service> = ArrayList()
        private var biodataSubscribeParams: BiodataSubscribeParams? = null
        private var enterAffectiveCloudConfig: EnterAffectiveCloudConfig? = null
        private var enterAffectiveCloudManager: EnterAffectiveCloudManager? = null
        private var rawJsonResponseFunction: Function1<String, Unit>? = null
        private var rawJsonRequestFunction: Function1<String, Unit>? = null
        private var affectiveSubscribeParams: AffectiveSubscribeParams? = null

        @BeforeClass
        @JvmStatic
        fun init() {
            PowerMockito.mockStatic(Log::class.java)
            availableBioServices.add(Service.PEPR)

            availableAffectiveServices.add(Service.PRESSURE)
            availableAffectiveServices.add(Service.COHERENCE)
            biodataSubscribeParams = BiodataSubscribeParams.Builder()
                .requestPEPR()
                .build()

            affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
                .requestPressure()
                .requestCoherence()
                .build()
            var algorithmParams = AlgorithmParams.Builder()
                .build()
            var storageSettings =
                StorageSettings.Builder().channelNum(AlgorithmParams.ChannelNum.CHANNEL_NUM_8)
                    .build()
            enterAffectiveCloudConfig =
                EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
                    .url(websocketAddress)
                    .availableBiodataServices(availableBioServices)
                    .availableAffectiveServices(availableAffectiveServices)
                    .affectiveSubscribeParams(affectiveSubscribeParams!!)
                    .biodataSubscribeParams(biodataSubscribeParams!!)
                    .algorithmParams(algorithmParams)
                    .build()
            enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig!!)
            rawJsonResponseFunction = fun(s: String): Unit {
                println(s)
            }
            rawJsonRequestFunction = fun(s: String): Unit {
//                                System.out.println("send msg is "+s);
            }
            enterAffectiveCloudManager!!.addRawJsonResponseListener(rawJsonResponseFunction!!)
            enterAffectiveCloudManager!!.addRawJsonRequestListener(rawJsonRequestFunction!!)
            val countDownLatch = CountDownLatch(1)
            val isSuccess = booleanArrayOf(false)

            enterAffectiveCloudManager!!.init(object : Callback {
                override fun onSuccess() {
                    isSuccess[0] = true
                    countDownLatch.countDown()
                }

                override fun onError(error: Error?) {
                    isSuccess[0] = false
                }
            })
            try {
                countDownLatch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            assertEquals(true, isSuccess[0])
        }

        @AfterClass
        @JvmStatic
        fun release() {
            PowerMockito.mockStatic(Log::class.java)
            val results = BooleanArray(1)
            val countDownLatch = CountDownLatch(1)
            enterAffectiveCloudManager!!.release(object : Callback {
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

            enterAffectiveCloudManager!!.removeRawJsonResponseListener(rawJsonResponseFunction!!)
            enterAffectiveCloudManager!!.removeRawJsonRequestListener(rawJsonRequestFunction!!)
            assertEquals(true, results[0])
        }
    }
}