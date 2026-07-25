package com.ryosoftware.caffeine_tile

import android.content.Context

class Utils {
    companion object {
        fun getLabelForInterval(context: Context, seconds: Int): String {
            if (seconds <= 0) return context.getString(R.string.unlimited)

            val resources = context.resources

            if (seconds < 60) return resources.getQuantityString(R.plurals.seconds, seconds, seconds)

            val minutes = seconds / 60
            val remainingSeconds = seconds % 60

            val minutesString = resources.getQuantityString(R.plurals.minutes, minutes, minutes)

            return if (remainingSeconds > 0) {
                val secondsString = resources.getQuantityString(R.plurals.seconds, remainingSeconds, remainingSeconds)
                context.getString(R.string.minutes_and_seconds, minutesString, secondsString)
            } else {
                minutesString
            }
        }
    }
}