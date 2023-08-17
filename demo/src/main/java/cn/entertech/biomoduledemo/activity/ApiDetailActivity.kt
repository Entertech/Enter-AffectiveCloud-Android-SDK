package cn.entertech.biomoduledemo.activity

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.affectivecloudsdk.AffectiveSubscribeParams
import cn.entertech.affectivecloudsdk.BiodataSubscribeParams
import cn.entertech.affectivecloudsdk.EnterAffectiveCloudApiFactory
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.BaseApi
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import cn.entertech.biomoduledemo.R
import cn.entertech.ble.single.BiomoduleBleManager
import com.orhanobut.logger.Logger
import org.java_websocket.handshake.ServerHandshake
import java.io.File
import java.lang.Exception
import java.util.*
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.Service

class ApiDetailActivity : AppCompatActivity() {

    private var mEnterAffectiveCloudApi: BaseApi? = null
    private lateinit var biomoduleBleManager: BiomoduleBleManager

    private var affectiveSubscribeParams: AffectiveSubscribeParams? = null
    private var biodataSubscribeParams: BiodataSubscribeParams? = null
    /* Need to apply  :APP_KEY、APP_SECRET、USER_NAME*/
    val APP_KEY: String = "6eabf68e-760e-11e9-bd82-0242ac140006"
    val APP_SECRET: String = "68a09cf8e4e06718b037c399f040fb7e"
    val USER_NAME: String = "test1"
    /*userId:your email or phone num;自己的用户ID：邮箱或者手机号码*/
    val USER_ID: String = "123456789@qq.com"
    var saveRootPath: String = Environment.getExternalStorageDirectory().path + File.separator + "biorawdata"
    var saveEEGPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata" + File.separator + "eeg" + File.separator
    var saveHRPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata" + File.separator + "hr" + File.separator
    var fileName: String = ""
    var availableAffectiveServices = listOf(Service.ATTENTION, Service.PRESSURE, Service.AROUSAL, Service.SLEEP)
    var availableBioServices = listOf(Service.EEG, Service.HR)
    var websocketAddress = "wss://server.affectivecloud.cn/ws/algorithm/v1/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_detail)

        biomoduleBleManager = BiomoduleBleManager.getInstance(this)
        biomoduleBleManager.addRawDataListener(rawListener)
        biomoduleBleManager.addHeartRateListener(heartRateListener)
        mEnterAffectiveCloudApi =
            EnterAffectiveCloudApiFactory.createApi(websocketAddress, APP_KEY, APP_SECRET, USER_ID)

        biodataSubscribeParams = BiodataSubscribeParams.Builder()
            .requestHR()
            .requestEEG()
            .build()

        affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestSleep()
            .requestAttention()
            .requestRelaxation()
            .requestPressure()
            .requestPleasure()
            .build()
    }

    var rawListener = fun(bytes: ByteArray) {
        mEnterAffectiveCloudApi?.appendEEGData(bytes)
    }

    var heartRateListener = fun(heartRate: Int) {
        mEnterAffectiveCloudApi?.appendHeartData(heartRate)
    }


    fun onConnectSocket(@Suppress("UNUSED_PARAMETER")view: View) {
        Logger.d("正在连接情感云平台...")
        mEnterAffectiveCloudApi?.openWebSocket(object : WebSocketCallback {
            override fun onOpen(serverHandshake: ServerHandshake?) {
                Logger.d("情感云平台连接成功!")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Logger.d("情感云已断开连接：$reason")
            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
                Logger.d("情感云连接异常：${e.toString()}")
            }

        })
    }

    fun onSessionCreate(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.createSession(object :
            cn.entertech.affective.sdk.api.Callback2<String> {
            override fun onSuccess(t: String?) {
                Logger.d("情感云Session已创建，session id:$t")
            }

            override fun onError(error: Error?) {
                Logger.d("情感云Session创建异常:${error.toString()}")
            }
        })
    }

    fun onSessionClose(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.destroySessionAndCloseWebSocket(object :
            cn.entertech.affective.sdk.api.Callback {
            override fun onSuccess() {
                Logger.d("情感云会话已关闭")
            }

            override fun onError(error: Error?) {
                Logger.d("情感云会话失败：${error.toString()}")
            }
        })
    }

    fun onSessionRestore(@Suppress("UNUSED_PARAMETER")view: View) {
        mEnterAffectiveCloudApi?.restore(object : cn.entertech.affective.sdk.api.Callback {
            override fun onSuccess() {
                Logger.d("情感云会话已重连")
            }

            override fun onError(error: Error?) {
                Logger.d("情感云会话重连失败：${error.toString()}")
            }

        })
    }

    fun onInitBiodataServer(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.initBiodataServices(availableBioServices, object :
            cn.entertech.affective.sdk.api.Callback {
            override fun onSuccess() {
                Logger.d("情感云基础服务已初始化成功")
                fileName = "${System.currentTimeMillis()}.txt"
//                FileHelper.getInstance().setEEGPath(saveEEGPath + fileName)
//                FileHelper.getInstance().setHRPath(saveHRPath + fileName)
            }

            override fun onError(error: Error?) {
                Logger.d("情感云基础服务初始化失败：${error.toString()}")
            }

        })
    }

    fun onUploadBiodata(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        biomoduleBleManager.startHeartAndBrainCollection()
    }

    fun onStopCollect(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        biomoduleBleManager.stopHeartAndBrainCollection()
    }


    fun onStartContact(@Suppress("UNUSED_PARAMETER")view: View) {
        biomoduleBleManager.startContact()
    }

    fun onStopContact(@Suppress("UNUSED_PARAMETER")view: View) {
        biomoduleBleManager.stopContact()
    }

    fun onSubscribeBiodata(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.subscribeBioData(biodataSubscribeParams!!, object :
            cn.entertech.affective.sdk.api.Callback2<cn.entertech.affective.sdk.bean.RealtimeBioData> {
            override fun onSuccess(t: cn.entertech.affective.sdk.bean.RealtimeBioData?) {
                Logger.d("基础服务实时数据：${t.toString()}")
            }

            override fun onError(error: Error?) {
                Logger.d("实时数据返回异常：${error.toString()}")
            }

        }, object : cn.entertech.affective.sdk.api.Callback2<SubBiodataFields> {
            override fun onSuccess(t: SubBiodataFields?) {
                Logger.d("基础服务订阅成功，当前已订阅内容：${t.toString()}")
            }

            override fun onError(error: Error?) {
                Logger.d("基础服务订阅失败：${error.toString()}")
            }

        })
    }

    fun onBiodataReport(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.getBiodataReport(availableBioServices, object :
            cn.entertech.affective.sdk.api.Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                Logger.d("基础服务报表：${t.toString()}")
            }

            override fun onError(error: Error?) {
                Logger.d("获取基础服务报表：${error.toString()}")
            }

        })
    }

    fun onUnsubscribeBiodata(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk() || biodataSubscribeParams == null) {
            return
        }
        mEnterAffectiveCloudApi?.unsubscribeBioData(biodataSubscribeParams!!, object :
            cn.entertech.affective.sdk.api.Callback2<SubBiodataFields> {
            override fun onSuccess(t: SubBiodataFields?) {
                Logger.d("基础服务取消订阅成功，当前已订阅内容：${t.toString()}")
            }

            override fun onError(error: Error?) {
                Logger.d("基础服务取消订阅失败：${error.toString()}")
            }
        })
    }

    fun onStartAffective(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.initAffectiveDataServices(availableAffectiveServices,
            object : cn.entertech.affective.sdk.api.Callback {
                override fun onSuccess() {
                    Logger.d("情感服务已开启")
                }

                override fun onError(error: Error?) {
                    Logger.d("情感服务开启失败：$error")
                }

            })
    }


    fun onSubscribeAffective(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.subscribeAffectiveData(affectiveSubscribeParams!!,
            object :
                cn.entertech.affective.sdk.api.Callback2<cn.entertech.affective.sdk.bean.RealtimeAffectiveData> {
                override fun onSuccess(t: cn.entertech.affective.sdk.bean.RealtimeAffectiveData?) {
                    Logger.d("实时情感数据：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    Logger.d("情感数据返回异常：${error.toString()}")
                }

            },
            object : cn.entertech.affective.sdk.api.Callback2<SubAffectiveDataFields> {
                override fun onSuccess(t: SubAffectiveDataFields?) {
                    Logger.d("情感服务订阅成功，当前已订阅内容：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    Logger.d("情感服务订阅失败：${error.toString()}")
                }

            })
    }


    fun onAffectiveReport(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.getAffectivedataReport(availableAffectiveServices,
            object : cn.entertech.affective.sdk.api.Callback2<HashMap<Any, Any?>> {
                override fun onSuccess(t: HashMap<Any, Any?>?) {
                    Logger.d("情感报表数据：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    Logger.d("获取情感报表数据失败：${error.toString()}")
                }

            })
    }


    fun onUnsubscribeAffective(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk() || affectiveSubscribeParams == null) {
            return
        }
        mEnterAffectiveCloudApi?.unsubscribeAffectiveData(affectiveSubscribeParams!!,
            object : cn.entertech.affective.sdk.api.Callback2<SubAffectiveDataFields> {
                override fun onSuccess(t: SubAffectiveDataFields?) {
                    Logger.d("情感服务取消订阅成功，当前已订阅内容：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    Logger.d("情感服务取消订阅失败：${error.toString()}")
                }
            })
    }

    fun onFinishAffective(@Suppress("UNUSED_PARAMETER")view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.finishAllAffectiveDataServices(object :
            cn.entertech.affective.sdk.api.Callback {
            override fun onSuccess() {
                Logger.d("情感服务已结束")
            }

            override fun onError(error: Error?) {
                Logger.d("情感服务结束失败")
            }

        })
    }

    fun isStatusOk(): Boolean {
        if (!biomoduleBleManager.isConnected() || !mEnterAffectiveCloudApi!!.isWebSocketOpen()) {
            Toast.makeText(this, "Ble or Socket Disconnected!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


}
