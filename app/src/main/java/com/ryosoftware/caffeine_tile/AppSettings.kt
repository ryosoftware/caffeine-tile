package com.ryosoftware.caffeine_tile

import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import kotlin.jvm.java

object AppSettings {
    private const val PREFS_NAME = "caffeine_prefs"
    private const val KEY_ORIGINAL_TIMEOUT = "original_screen_off_timeout"
    private const val KEY_DEFAULT_TIMEOUT = "default_timeout"

    fun getPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    private fun sendLocalExplicitBroadcast(context: Context, intent: Intent) {
        intent.apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }

    private fun setScreenTimeout(context: Context, value: Int): Int {
        val originalTimeout = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            -1
        )

        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            value
        )

        val currentTimeout = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            -1
        )

        Log.d(BuildConfig.TAG, "Restoring screen timeout; original=${originalTimeout/1000}, current=${currentTimeout/1000}")

        return originalTimeout
    }

    fun setToggleActivityState(context: Context, preventingScreenOff: Boolean) {
        val canWriteSystemSettings = canWriteSystemSettings(context)

        val packageManager = context.packageManager

        val caffeineOnActivityEnabled = canWriteSystemSettings && preventingScreenOff
        packageManager.setComponentEnabledSetting(
            ComponentName(context, "${BuildConfig.APPLICATION_ID}.CaffeineOnActivity"),
            if (caffeineOnActivityEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        val caffeineOffActivityEnabled = canWriteSystemSettings && (!preventingScreenOff)
        packageManager.setComponentEnabledSetting(
            ComponentName(context, "${BuildConfig.APPLICATION_ID}.CaffeineOffActivity"),
            if (caffeineOffActivityEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun setToggleActivityState(context: Context) =
        setToggleActivityState(context, isPreventingScreenOff(context))

    fun canWriteSystemSettings(context: Context) =
        Settings.System.canWrite(context)

    private fun isPreventingScreenOff(prefs: SharedPreferences) =
        prefs.contains(KEY_ORIGINAL_TIMEOUT)

    fun isPreventingScreenOff(context: Context) =
        isPreventingScreenOff(getPreferences(context))

    private fun vibrate(context: Context, vibrationPattern: LongArray?) {
        if (vibrationPattern != null) {
            val vibrator = context.getSystemService(Vibrator::class.java)

            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
            }
        }
    }

    private fun preventScreenOff(context: Context, prefs: SharedPreferences, seconds: Int) {
        if (isPreventingScreenOff(prefs)) return


        prefs.edit {
            val timeout = if (seconds <= 0) Int.MAX_VALUE else (seconds * DateUtils.SECOND_IN_MILLIS).toInt()
            val originalTimeout = setScreenTimeout(context, timeout)
            putInt(KEY_ORIGINAL_TIMEOUT, originalTimeout)
        }

        val message = if (seconds <= 0) context.getString(R.string.caffeine_on_toast)
                      else context.getString(R.string.caffeine_on_toast_with_time, Utils.getLabelForInterval(context, seconds))
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        sendLocalExplicitBroadcast(context, Intent(CaffeineTileService.ACTION_UPDATE_TILE))

        context.startForegroundService(Intent(context, CaffeineService::class.java))

        setToggleActivityState(context,true)

        vibrate(context, longArrayOf(0, 120, 80, 120))
    }

    fun preventScreenOff(context: Context, seconds: Int) =
        preventScreenOff(context, getPreferences(context), seconds)

    fun restoreUserScreenControl(context: Context, prefs: SharedPreferences) {
        if (!isPreventingScreenOff(prefs)) return

        prefs.edit {
            val originalTimeout = prefs.getInt(KEY_ORIGINAL_TIMEOUT, 30)
            remove(KEY_ORIGINAL_TIMEOUT)
            setScreenTimeout(context, originalTimeout)
        }

        Toast.makeText(context, R.string.caffeine_off_toast, Toast.LENGTH_LONG).show()

        sendLocalExplicitBroadcast(context,Intent(CaffeineTileService.ACTION_UPDATE_TILE))

        sendLocalExplicitBroadcast(context, Intent(CaffeineService.ACTION_STOP_SERVICE))

        setToggleActivityState(context,false)

        vibrate(context, longArrayOf(0, 30))
    }

    fun restoreUserScreenControl(context: Context) =
        restoreUserScreenControl(context, getPreferences(context))

    fun toggleUserScreenControl(context: Context) {
        val prefs = getPreferences(context)

        if (isPreventingScreenOff(prefs)) {
            restoreUserScreenControl(context, prefs)
        } else {
            val seconds = getDefaultTimeout(context)
            preventScreenOff(context, prefs, seconds)
        }
    }

    fun getDefaultTimeout(context: Context): Int =
        getPreferences(context).getInt(KEY_DEFAULT_TIMEOUT, 0)

    fun setDefaultTimeout(context: Context, seconds: Int) =
        getPreferences(context).edit { putInt(KEY_DEFAULT_TIMEOUT, seconds) }
}
