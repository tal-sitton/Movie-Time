package com.example.movietime

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.time.LocalDateTime

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val now: LocalDateTime = LocalDateTime.now()
        val json: JSONObject? = JSONUtils.loadJSONFromFile(this)
        if (json != null) {
            val lastUpdate = json.getString("time")
            if (lastUpdate.split("-")[0].toInt() != now.dayOfMonth) {
                findViewById<ProgressBar>(R.id.progressBar).visibility = ProgressBar.VISIBLE
                JSONUtils.updateJSON(this)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    this.startActivity(Intent(this, MainActivity::class.java))
                }, 15)
            }
        } else {
            findViewById<ProgressBar>(R.id.progressBar).visibility = ProgressBar.VISIBLE
            JSONUtils.updateJSON(this)
        }
    }
}
