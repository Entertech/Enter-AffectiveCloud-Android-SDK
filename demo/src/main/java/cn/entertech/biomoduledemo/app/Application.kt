package cn.entertech.biomoduledemo.app

import android.app.Application

class Application:android.app.Application() {

    companion object {
        var application: Application? = null
        fun getInstance(): Application {
            return application!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }


}