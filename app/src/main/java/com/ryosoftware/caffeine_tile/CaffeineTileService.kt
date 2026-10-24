package com.ryosoftware.caffeine_tile

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class CaffeineTileService : TileService() {
    override fun onStartListening() {
        instance = this
        updateTile()
    }

    override fun onStopListening() {
        instance = null
        super.onStopListening()
    }

    override fun onClick() {
        if (!AppSettings.canWriteSystemSettings(this)) {
            Toast.makeText(this, R.string.missing_permissions, Toast.LENGTH_LONG).show()
            updateTile()
        } else if (isLocked) {
            unlockAndRun { AppSettings.toggleUserScreenControl(this) }
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
        private var instance: CaffeineTileService? = null

        fun updateTile(context: Context) {
            if (instance == null) {
                val component = ComponentName(context.packageName, CaffeineTileService::class.java.name)
                requestListeningState(context, component) }
            else {
                instance?.updateTile()
            }
        }
    }
}
