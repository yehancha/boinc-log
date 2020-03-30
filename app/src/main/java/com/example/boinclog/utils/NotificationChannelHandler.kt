package com.example.boinclog.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val CHANNEL_ID_BOINC_NOTIFICATIONS = "CHANNEL_ID_BOINC_NOTIFICATIONS"

private const val NOTIFICATION_CHANNEL_NAME = "Boinc Notification Channel"

class NotificationChannelHandler {
    companion object {
        fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = NOTIFICATION_CHANNEL_NAME
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID_BOINC_NOTIFICATIONS, name, importance)
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}