package com.example.movietime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : MyTemplateActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val allowDubbedSetting = getSharedPreferences("preferences", MODE_PRIVATE)
        val allowDubbedButton: SwitchCompat = findViewById(R.id.allowDubbed)

        allowDubbedButton.isChecked = allowDubbedSetting.getBoolean("allowDubbed", true)

        allowDubbedButton.setOnCheckedChangeListener { _, isChecked ->
            val editor = allowDubbedSetting.edit()
            editor.putBoolean("allowDubbed", isChecked)
            editor.apply()
            recreate()
        }

        val mainActivityButton: Button = findViewById(R.id.backButton)
        mainActivityButton.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        val supportButton: ImageView = findViewById(R.id.supportButton)
        supportButton.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://tal-sitton.github.io/Movie-Time/"))
            startActivity(browserIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            onBackPressedCallback.handleOnBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
