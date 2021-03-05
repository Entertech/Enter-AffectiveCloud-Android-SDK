package cn.entertech.biomoduledemo.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.app.Constant
import cn.entertech.biomoduledemo.utils.SettingManager
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private var setting: SettingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        setting = SettingManager.getInstance()
        var appKey = setting?.appKey
        var appSecret = setting?.appSecret
        if (appKey != null && appKey != "") {
            et_app_key.setText(appKey)
        }
        if (appSecret != null && appSecret != "") {
            et_app_secret.setText(appSecret)
        }
    }

    fun onContinue(@Suppress("UNUSED_PARAMETER")view: View) {
        var appKey = et_app_key.text
        var appSecret = et_app_secret.text
        if (!appKey.isNullOrEmpty() && !appSecret.isNullOrEmpty()) {
            setting?.appKey = appKey.toString()
            setting?.appSecret = appSecret.toString()
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constant.INTENT_APP_KEY, setting?.appKey)
            intent.putExtra(Constant.INTENT_APP_SECRET, setting?.appSecret)
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.auth_page_title), Toast.LENGTH_SHORT).show()
        }
    }
}
