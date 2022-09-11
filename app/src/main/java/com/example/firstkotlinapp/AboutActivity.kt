package com.example.firstkotlinapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class AboutActivity : MyTemplateActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        val mainActivityButton: Button = findViewById(R.id.backButton)
        mainActivityButton.setOnClickListener {
            startActivity(intent)
        }

        val supportButton: ImageView = findViewById(R.id.supportButton)
        supportButton.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://tal-sitton.github.io/Movie-Time/"))
            startActivity(browserIntent)
        }
    }
}
