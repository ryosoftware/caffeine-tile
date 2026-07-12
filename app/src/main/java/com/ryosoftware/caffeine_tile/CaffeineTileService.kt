package com.ryosoftware.caffeine_tile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class CaffeineTileService : TileService() {
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
        if (!AppSettings.canWriteSystemSettings(this)) {
            Toast.makeText(this, R.string.missing_permissions, Toast.LENGTH_LONG).show()
            updateTile()
        } else {
            AppSettings.toggleUserScreenControl(this)
        }
    }

    private fun updateTile() {
        if (!AppSettings.canWriteSystemSettings(this)) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.label = ""
        } else if (AppSettings.isPreventingScreenOff(this)) {
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.label = getString(R.string.caffeine_on_tile)
        } else {
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.label = getString(R.string.caffeine_off_tile)
        }

        qsTile.updateTile()
    }

    companion object {
        const val ACTION_UPDATE_TILE = "${BuildConfig.APPLICATION_ID}.UPDATE_TILE"
    }
}
