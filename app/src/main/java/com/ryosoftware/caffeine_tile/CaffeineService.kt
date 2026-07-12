package com.ryosoftware.caffeine_tile

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CaffeineService : Service() {
    inner class ScreenStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF,
                ACTION_STOP_SERVICE -> {
                    unregisterReceiverSafely()

                    AppSettings.restoreUserScreenControl(context)

                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private var isReceiverRegistered = false
    private val screenStateReceiver = ScreenStateReceiver()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun registerReceiver() {
        if (! isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(ACTION_STOP_SERVICE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenStateReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(screenStateReceiver, filter)
            }

            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiverSafely() {
        if (isReceiverRegistered) {
            runCatching { unregisterReceiver(screenStateReceiver) }
            isReceiverRegistered = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        registerReceiver()

        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiverSafely()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("LaunchActivityFromNotification")
    private fun buildNotification(): Notification {
        val deactivateIntent = Intent(ACTION_STOP_SERVICE).apply {
            setPackage(packageName)
        }

        val deactivatePendingIntent = PendingIntent.getBroadcast(
            this, 0, deactivateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, getString(R.string.deactivate), deactivatePendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, getString(R.string.foreground_channel_name), NotificationManager.IMPORTANCE_LOW).apply {
            description = getString(R.string.foreground_channel_desc)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "caffeine_channel"

        const val ACTION_STOP_SERVICE = "${BuildConfig.APPLICATION_ID}.STOP_CAFFEINE"
    }
}
