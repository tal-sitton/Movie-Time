package com.example.firstkotlinapp

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    companion object {
        var allScreenings: MutableList<Screening> = mutableListOf()
    }

    private fun jsonToList() {
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        val json: JSONObject? = Utils.loadJSONFromAsset(this)
        print(json)
        if (json != null) {
            val screenings = json.getJSONArray("Screenings")
            for (i in 0 until screenings.length()) {
                val screeningInfo = screenings.getJSONObject(i)
                val date = screeningInfo.getString("date")
                val theater = screeningInfo.getString("cinema")
                val location = screeningInfo.getString("location")
                val title = screeningInfo.getString("title")
                val type = screeningInfo.getString("type")
                val time = screeningInfo.getString("time")
                val url = screeningInfo.getString("link")

                val screening = Screening(title, date, time, location, theater, type, url)
                allScreenings.add(screening)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jsonToList()

        val movieButton: Button = findViewById(R.id.movieButton)
        val dateButton: Button = findViewById(R.id.dateButton)
        val cinemaButton: Button = findViewById(R.id.cinemaButton)

        val intent = Intent()
        intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)

        movieButton.setOnClickListener {
            intent.setClass(this, MovieActivity::class.java)
            toast("Movie Button Clicked")
            startActivity(intent)
        }

        dateButton.setOnClickListener {
            intent.setClass(this, DateActivity::class.java)
            toast("Date Button Clicked")
            startActivity(intent)
        }
        cinemaButton.setOnClickListener {
            intent.setClass(this, CinemaActivity::class.java)
            toast("Cinema Button Clicked")
            startActivity(intent)
        }

        createButtons(findViewById(R.id.gl))
    }

    private fun createButtons(grid: GridLayout) {
        var i = 1
        for (screening in allScreenings) {
            val button = screening.createButton(this)
            grid.addView(button)
            button.setOnClickListener {
                toast("Button clicked")
                println("BUTTON HEIGHT: ${button.height}")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(screening.url))
                startActivity(browserIntent)
            }
            if (i % 3 != 0) {
                val spacer = Space(this)
                spacer.layoutParams = ViewGroup.LayoutParams(8, 500)
                grid.addView(spacer)
            }
            i++
        }
    }

    override fun onRestart() {
        super.onRestart()
        toast("Movies: ${MovieActivity.selectedMovies}")
        toast("Cinemas: ${CinemaActivity.selectedCinemas}")
    }

    private fun toast(message: String) {
        println(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}