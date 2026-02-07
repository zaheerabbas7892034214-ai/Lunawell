package com.zaheer.lunawell.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationUtils {
    
    const val CHANNEL_ID_REMINDER = "reminder_channel"
    const val CHANNEL_ID_PERIOD = "period_channel"
    
    private const val CHANNEL_NAME_REMINDER = "Reminders"
    private const val CHANNEL_DESC_REMINDER = "Notifications for medication and appointment reminders"
    
    private const val CHANNEL_NAME_PERIOD = "Period Tracking"
    private const val CHANNEL_DESC_PERIOD = "Notifications for period and fertile window tracking"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                CHANNEL_NAME_REMINDER,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_REMINDER
            }
            
            val periodChannel = NotificationChannel(
                CHANNEL_ID_PERIOD,
                CHANNEL_NAME_PERIOD,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_PERIOD
            }
            
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(periodChannel)
        }
    }
    
    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)
    }
}
