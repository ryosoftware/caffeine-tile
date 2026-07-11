package com.ryosoftware.caffeine_tile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.android.material.color.DynamicColors
import com.ryosoftware.caffeine_tile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnWriteSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }

        @SuppressLint("BatteryLife")
        binding.btnBatteryOptimization.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }

        binding.btnPostNotifications.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }

        binding.lblVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        binding.lblVersion.setOnClickListener{ openGithubRepo() }

        binding.lblName.setOnClickListener{ openGithubRepo() }
    }

    private fun openGithubRepo() =
        startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.github_repo).toUri()))

    override fun onResume() {
        super.onResume()
        updatePermissionButtons()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            updatePermissionButtons()
        }
    }

    private fun updatePermissionButtons() {
        val canWriteSettings = Settings.System.canWrite(this)
        binding.btnWriteSettings.isEnabled = !canWriteSettings

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
        binding.btnBatteryOptimization.isEnabled = !isIgnoringBatteryOptimizations

        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val notificationGranted = needsNotificationPermission && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        binding.lblPostNotificationsTitle.isVisible = needsNotificationPermission
        binding.lblPostNotificationsDescription.isVisible = needsNotificationPermission
        binding.btnPostNotifications.isVisible = needsNotificationPermission
        binding.btnPostNotifications.isEnabled = !notificationGranted
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }
}
