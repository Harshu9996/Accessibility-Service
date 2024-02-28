package com.example.onlyusableassignment.services


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.onlyusableassignment.R


class FloatingButtonService : Service() {

    val TAG = "FloatingButtonService"
    private lateinit var windowManager: WindowManager
    private lateinit var button: Button
    private lateinit var sharedPref:SharedPreferences

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: called")
        sharedPref = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
        //initializing window manager and button
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        button = Button(this)
        button.text = getString(R.string.show_views)

        //Set service running status to false
        sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled),true).apply()

        button.setOnClickListener{
            //Toggling the status of running service

            val isServiceEnabled = !it.isSelected
            sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled),isServiceEnabled).apply()

            //starting the accessibility service
            val intent = Intent(this,MyAccessibilityService::class.java)
            startService(intent)



        }
        setWindowManager(button)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: called")
        start()
        return START_STICKY
    }


    private fun setWindowManager(button: Button){
        //Parameters for floating button view
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            }else{
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        //Set gravity to No Position to position it anywhere on the window
        params.gravity = Gravity.TOP or Gravity.START


        //Add button to windowManager
        windowManager.addView(button,params)
    }

    private fun start(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val notification = NotificationCompat.Builder(this,getString(R.string.foreground_Notification_channel_id)).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.foreground_service_title)).setContentText(getString(R.string.foreground_service_text)).build()
            startForeground(1,notification)
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
        //No communicationBinder is set
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        //Stop the service
        Log.d(TAG, "onDestroy: called")
        sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled),false).apply()

            val intent = Intent(this,MyAccessibilityService::class.java)
            stopService(intent)
            Log.d(TAG, "onDestroy: Stopped Accessibility service")
        if(button.isAttachedToWindow){
            windowManager.removeView((button))
        }

    }


}