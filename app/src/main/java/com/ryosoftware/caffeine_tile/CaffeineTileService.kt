package com.ryosoftware.caffeine_tile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class CaffeineTileService : TileService() {
    private lateinit var prefs: SharedPreferences

    inner class UpdateTileReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_UPDATE_TILE -> {
                    updateTile()
                }
            }
        }
    }

    private val updateTileReceiver = UpdateTileReceiver()
    private var isReceiverRegistered = false

    override fun onCreate() {
        super.onCreate()
        prefs = Settings.getPreferences(this)
    }

    override fun onDestroy() {
        unregisterReceiverSafely()
        super.onDestroy()
    }

    override fun onStartListening() {
        registerReceiver()
        updateTile()
    }

    override fun onStopListening() {
        unregisterReceiverSafely()
        super.onStopListening()
    }

    private fun registerReceiver() {
        if (! isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(ACTION_UPDATE_TILE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(updateTileReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(updateTileReceiver, filter)
            }

            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiverSafely() {
        if (isReceiverRegistered) {
            runCatching { unregisterReceiver(updateTileReceiver) }
            isReceiverRegistered = false
        }
    }

    override fun onClick() {
        if (!Settings.canWrite(this)) {
            Toast.makeText(this, R.string.missing_permissions, Toast.LENGTH_LONG).show()
            return
        }

        if (qsTile.state == Tile.STATE_ACTIVE) { deactivate() } else { activate() }
    }

    private fun activate() {
        Settings.preventScreenOff(this, prefs)

        startForegroundService(Intent(this, CaffeineService::class.java))

        qsTile.state = Tile.STATE_ACTIVE
        qsTile.updateTile()
    }

    private fun deactivate() {
        Settings.restoreUserTimeout(this, prefs)

        sendBroadcast(Intent(CaffeineService.ACTION_STOP_CAFFEINE))

        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    private fun updateTile() {
        qsTile.state = when {
            !Settings.canWrite(this) -> Tile.STATE_UNAVAILABLE
            Settings.isPreventingScreenOff(this, prefs) -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }

        qsTile.updateTile()
    }

    companion object {
        const val ACTION_UPDATE_TILE = "${BuildConfig.APPLICATION_ID}.UPDATE_TILE"
    }
}
