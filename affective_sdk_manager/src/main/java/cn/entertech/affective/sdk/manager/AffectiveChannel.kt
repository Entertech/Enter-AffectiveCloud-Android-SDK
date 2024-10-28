package cn.entertech.affective.sdk.manager

import android.app.NotificationManager
import cn.entertech.base.notification.INotificationChannelType

object AffectiveChannel : INotificationChannelType {
    override fun getChannelDescription(): String {
        return ""
    }

    override fun getChannelId(): String {
        return "Affective"
    }

    override fun getChannelImportance(): Int {
        return NotificationManager.IMPORTANCE_HIGH
    }

    override fun getChannelTypeName(): String {
        return "Affective"
    }
}