package com.example.firstkotlinapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.time.LocalDateTime

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val now: LocalDateTime = LocalDateTime.now()
        val json: JSONObject? = Utils.loadJSONFromAsset(this)
        if (json != null) {
            val lastUpdate = json.getString("time")
            if (lastUpdate.split("-")[0].toInt() != now.dayOfMonth) {
                Utils.updateJSON(this)
            }
        } else {
            Utils.updateJSON(this)
        }
    }
}