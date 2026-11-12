package cn.entertech.biomoduledemo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.utils.SettingManager
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val settings by lazy { SettingManager.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        et_app_key.setText(settings.appKey)
        et_app_secret.setText(settings.appSecret)
        et_user_id.setText(settings.userId)
    }

    fun onContinue(@Suppress("UNUSED_PARAMETER") view: View) {
        val appKey = et_app_key.text.toString().trim()
        val appSecret = et_app_secret.text.toString().trim()
        val userId = et_user_id.text.toString().trim()
        if (appKey.isBlank() || appSecret.isBlank() || userId.isBlank()) {
            Toast.makeText(this, getString(R.string.auth_page_title), Toast.LENGTH_SHORT).show()
            return
        }

        settings.saveCredentials(appKey, appSecret, userId)
        startActivity(Intent(this, MainActivity::class.java))
    }
}
