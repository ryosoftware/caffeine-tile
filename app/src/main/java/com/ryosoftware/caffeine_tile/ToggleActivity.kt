package com.ryosoftware.caffeine_tile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ToggleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppSettings.canWriteSystemSettings(this)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            AppSettings.toggleUserScreenControl(this)
        }

        finish()
    }
}
