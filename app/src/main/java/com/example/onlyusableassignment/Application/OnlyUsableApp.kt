package com.example.onlyusableassignment.Application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import  com.example.onlyusableassignment.R

class OnlyUsableApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //Create Notification channel for foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =  NotificationChannel(getString(R.string.foreground_Notification_channel_id),getString(R.string.foreground_Notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}