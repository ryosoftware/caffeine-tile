package com.ryosoftware.caffeine_tile

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit

object Settings {
    const val PREFS_NAME = "caffeine_prefs"
    const val KEY_ORIGINAL_TIMEOUT = "original_screen_off_timeout"
    const val CAFFEINE_TIMEOUT_MS = Int.MAX_VALUE

    fun getPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    fun canWrite(context: Context): Boolean =
        Settings.System.canWrite(context)


    fun isPreventingScreenOff(context: Context, prefs: SharedPreferences) =
        prefs.contains(KEY_ORIGINAL_TIMEOUT)

    fun preventScreenOff(context: Context, prefs: SharedPreferences) {
        if (isPreventingScreenOff(context, prefs)) return

        val originalTimeout = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            CAFFEINE_TIMEOUT_MS
        )

        prefs.edit { putInt(KEY_ORIGINAL_TIMEOUT, originalTimeout) }

        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            CAFFEINE_TIMEOUT_MS
        )

        val currentTimeout = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            CAFFEINE_TIMEOUT_MS
        )

        Toast.makeText(context, R.string.caffeine_active, Toast.LENGTH_LONG).show()

        Log.d(BuildConfig.TAG, "Prevent screen off; original=${originalTimeout/1000}, current=${currentTimeout/1000}")
    }

    fun restoreUserTimeout(context: Context, prefs: SharedPreferences) {
        if (!isPreventingScreenOff(context, prefs)) return

        val originalTimeout = prefs.getInt(KEY_ORIGINAL_TIMEOUT, CAFFEINE_TIMEOUT_MS)

        prefs.edit { remove(KEY_ORIGINAL_TIMEOUT) }

        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            originalTimeout
        )

        val currentTimeout = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            CAFFEINE_TIMEOUT_MS
        )

        Toast.makeText(context, R.string.caffeine_deactive, Toast.LENGTH_LONG).show()

        Log.d(BuildConfig.TAG, "Restoring screen timeout; original=${originalTimeout/1000}, current=${currentTimeout/1000}")
    }
}
