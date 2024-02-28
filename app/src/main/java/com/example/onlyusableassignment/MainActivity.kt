package com.example.onlyusableassignment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.onlyusableassignment.services.FloatingButtonService
import com.example.onlyusableassignment.ui.MainScreen.MainScreen
import com.example.onlyusableassignment.ui.theme.OnlyUsableAssignmentTheme

class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Shared Preferences instance
        val sharedPref = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
        //Putting boolean running status of service to false
        sharedPref.edit().putBoolean(getString(R.string.isServiceEnabled),false).apply()

        //Ask Permission for showing Notifications
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),0)

        }



        setContent {
            var isServiceStarted = remember{
                mutableStateOf(false)
            }
            OnlyUsableAssignmentTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                   MainScreen(isServiceStarted = isServiceStarted, click = {
                       //Migrating user to give accessibility permission
                       val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                       startActivity(intent)
                       isServiceStarted.value = true
                   }, stopServiceClick = {
                       stopService()
                   })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Called On Resume")
        startService()
    }
    private fun startService(){
        //First check the permission
        //Check id current device is running on Android 6(API level 23) or higher
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            //Check overlay drawing permission
            if(Settings.canDrawOverlays(this)){
                //Have the Permission to draw
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    val intent = Intent(applicationContext,FloatingButtonService::class.java)
                    startForegroundService(intent)
                }else{
                    val intent = Intent(applicationContext,FloatingButtonService::class.java)
                    startService(intent)
                    Log.d(TAG, "startService: Build version is not O")
                }
            }else{
                // Not have permission
                askOverlayPermission()
//                val intent = Intent(this,FloatingButtonService::class.java)
//                startService(intent)
                Log.d(TAG, "startService: Not have permission")
            }
        }else{
            val intent = Intent(applicationContext,FloatingButtonService::class.java)
            startService(intent)
        }

    }

    private fun stopService(){
        val intent = Intent(applicationContext,FloatingButtonService::class.java)
        stopService(intent)
    }
    fun askOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(myIntent)
            }
        }
    }
}
