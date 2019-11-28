package cn.entertech.biomoduledemo.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
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

    fun onContinue(view: View) {
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
            Toast.makeText(this, "请输入正确的app key 或 app secret", Toast.LENGTH_SHORT).show()
        }
    }
}
