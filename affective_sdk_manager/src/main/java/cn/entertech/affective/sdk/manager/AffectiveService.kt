package cn.entertech.affective.sdk.manager

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import cn.entertech.base.notification.NotificationUtils

class AffectiveService : Service() {

    companion object {
        private const val TAG = "AffectiveService"
        private const val SERVICE_NOTIFICATION_TITLE = "title"
        private const val SERVICE_NOTIFICATION_CONTENT = "content"
        private const val SERVICE_NOTIFICATION_ICON = "icon"
    }

    class AffectiveBinder(private val service: AffectiveService) : Binder() {
        fun getAffectiveService(): AffectiveService {
            return service
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return AffectiveBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(SERVICE_NOTIFICATION_TITLE) ?: ""
        val content = intent?.getStringExtra(SERVICE_NOTIFICATION_CONTENT) ?: ""
        val icon = intent?.getIntExtra(SERVICE_NOTIFICATION_ICON, -1) ?: -1
        val notification = NotificationUtils.createNotification(
            this, NotificationUtils.createNotificationChannel(
                this, AffectiveChannel
            ), title, content, smallIcon = icon, pri = NotificationCompat.PRIORITY_DEFAULT
        )
        startForeground(1, notification)
        return START_STICKY
    }
}