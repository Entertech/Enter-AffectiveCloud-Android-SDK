package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.app.Constant.Companion.SERVICE_ATTENTION
import cn.entertech.biomoduledemo.app.Constant.Companion.SERVICE_PRESSURE
import cn.entertech.biomoduledemo.app.Constant.Companion.SERVICE_RELAXATION
import cn.entertech.biomoduledemo.websocket.WebSocketManager
import cn.entertech.ble.FlowtimeBleManager
import com.orhanobut.logger.Logger
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var socketManager: WebSocketManager
    private lateinit var flowtimeBleManager: FlowtimeBleManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        flowtimeBleManager = FlowtimeBleManager.getInstance(this)
        socketManager = WebSocketManager.getInstance()
        socketManager.connect()
        initPermission()
    }

    /**
     * Android6.0 auth
     */
    fun initPermission() {
        val needPermission = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val needRequestPermissions = ArrayList<String>()
        for (i in needPermission.indices) {
            if (ActivityCompat.checkSelfPermission(this, needPermission[i]) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissions.add(needPermission[i])
            }
        }
        if (needRequestPermissions.size != 0) {
            val permissions = arrayOfNulls<String>(needRequestPermissions.size)
            for (i in needRequestPermissions.indices) {
                permissions[i] = needRequestPermissions[i]
            }
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
        }
    }


    fun onConnect(view: View) {
        flowtimeBleManager.scanNearDeviceAndConnect(fun() {
            Logger.d("扫描设备成功")
        }, fun(mac: String) {
            Logger.d("连接成功$mac")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接成功", Toast.LENGTH_SHORT).show()
            }
        }) { msg ->
            Logger.d("连接失败")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接失败：${msg}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onDisconnet(view: View) {
        flowtimeBleManager.disConnect()
    }

    fun toAttention(view: View) {
        var intent = Intent(this, DataActivity::class.java)
        intent.putExtra("service", SERVICE_ATTENTION)
        startActivity(intent)
    }

    fun toRelaxation(view: View) {
        var intent = Intent(this, DataActivity::class.java)
        intent.putExtra("service", SERVICE_RELAXATION)
        startActivity(intent)
    }

    fun toPressure(view: View) {
        var intent = Intent(this, DataActivity::class.java)
        intent.putExtra("service", SERVICE_PRESSURE)
        startActivity(intent)
    }

}
