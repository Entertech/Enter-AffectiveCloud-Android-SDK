package cn.entertech.biomoduledemo.app

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

class Application:android.app.Application() {

    companion object {
        var application: Application? = null
        fun getInstance(): Application {
            return application!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "9eb2bbf00f", true);
        application = this
    }


}