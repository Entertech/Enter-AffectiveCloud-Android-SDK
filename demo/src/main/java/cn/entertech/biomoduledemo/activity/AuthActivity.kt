package cn.entertech.biomoduledemo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.app.Constant
import cn.entertech.biomoduledemo.utils.SettingManager

class AuthActivity : AppCompatActivity() {

    private var setting: SettingManager? = null
    private lateinit var appKeyView: EditText
    private lateinit var appSecretView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        appKeyView = findViewById(R.id.et_app_key)
        appSecretView = findViewById(R.id.et_app_secret)
        setting = SettingManager.getInstance()
        val appKey = setting?.appKey
        val appSecret = setting?.appSecret
        if (appKey != null && appKey != "") {
            appKeyView.setText(appKey)
        }
        if (appSecret != null && appSecret != "") {
            appSecretView.setText(appSecret)
        }
    }

    fun onContinue(@Suppress("UNUSED_PARAMETER")view: View) {
        val appKey = appKeyView.text
        val appSecret = appSecretView.text
        if (!appKey.isNullOrEmpty() && !appSecret.isNullOrEmpty()) {
            setting?.appKey = appKey.toString()
            setting?.appSecret = appSecret.toString()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constant.INTENT_APP_KEY, setting?.appKey)
            intent.putExtra(Constant.INTENT_APP_SECRET, setting?.appSecret)
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.auth_page_title), Toast.LENGTH_SHORT).show()
        }
    }
}
