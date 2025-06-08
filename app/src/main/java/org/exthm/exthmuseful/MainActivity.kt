package org.exthm.exthmuseful

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.exthm.exthmuseful.ui.screen.SettingsScreen
import org.exthm.exthmuseful.service.UsefulService
import org.exthm.exthmuseful.ui.theme.ExthmUsefulTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExthmUsefulTheme {
                SettingsScreen()
            }
        }

        if (checkPermissions()) {
            startUsefulService()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        // 相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        // 附近的设备权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        

        return if (permissions.isNotEmpty()) {
            // 请求
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var allGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }

            if (allGranted) {
                startUsefulService()
            } 
        }
    }

    private fun startUsefulService() {
        val serviceIntent = Intent(this, UsefulService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}