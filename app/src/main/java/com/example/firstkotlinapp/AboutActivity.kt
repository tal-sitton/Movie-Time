package com.example.firstkotlinapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView

class AboutActivity : MyTemplateActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val mainActivityButton: Button = findViewById(R.id.backButton)
        mainActivityButton.setOnClickListener {
            onBackPressed()
        }

        val supportButton: ImageView = findViewById(R.id.supportButton)
        supportButton.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://tal-sitton.github.io/Movie-Time/"))
            startActivity(browserIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                MainActivity.resetToDefault()
                MainActivity.filter()
                recreate()
                true
            }
            R.id.action_about -> {
                onBackPressed()
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }
}
