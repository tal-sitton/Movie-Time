package com.example.firstkotlinapp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.LocalDateTime

class DateActivity : AppCompatActivity() {
    /*
    TODO THIS ONE IS BIT DIFFICULT
     */
    companion object {
        val selectedDate: LocalDateTime = LocalDateTime.now()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter)

        val ll: LinearLayout = findViewById(R.id.ll)

    }

}
