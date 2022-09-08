package com.example.firstkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button

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
    }
}