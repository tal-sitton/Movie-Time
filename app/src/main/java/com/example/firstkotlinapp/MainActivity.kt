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
        var allScreenings: Set<Screening> = setOf()
        var filteredCinemaScreenings: Set<Screening> = setOf()
        var filteredMoviesScreenings: Set<Screening> = setOf()
        var filteredScreenings: Set<Screening> = setOf()
    }

    private fun jsonToList() {
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        val json: JSONObject? = Utils.loadJSONFromAsset(this)
        print(json)
        val tmpAllScreenings: MutableList<Screening> = mutableListOf()
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
                tmpAllScreenings.add(screening)
            }
        }
        allScreenings = tmpAllScreenings.toSet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jsonToList()
        resetToDefault()

        val movieButton: Button = findViewById(R.id.movieButton)
        val dateButton: Button = findViewById(R.id.dateButton)
        val cinemaButton: Button = findViewById(R.id.cinemaButton)

        val intent = Intent()
        intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)

        movieButton.setOnClickListener {
            intent.setClass(this, MovieActivity::class.java)
            startActivity(intent)
        }

        dateButton.setOnClickListener {
            intent.setClass(this, DateActivity::class.java)
            startActivity(intent)
        }
        cinemaButton.setOnClickListener {
            intent.setClass(this, CinemaActivity::class.java)
            startActivity(intent)
        }

        createButtons(findViewById(R.id.gl))
    }

    private fun resetToDefault() {
        filteredCinemaScreenings = allScreenings
        filteredMoviesScreenings = allScreenings
        filteredScreenings = allScreenings
        MovieActivity.selectedMovies.clear()
        CinemaActivity.selectedCinemas.clear()
    }

    private fun createButtons(grid: GridLayout) {
        grid.removeAllViewsInLayout()
        var i = 1
        for (screening in filteredScreenings) {
            val button = screening.createButton(this)
            grid.addView(button)
            button.setOnClickListener {
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
        val selectedMovies = MovieActivity.selectedMovies
        if (selectedMovies.isNotEmpty()) {
            filteredMoviesScreenings =
                allScreenings.filter { screening ->
                    selectedMovies.contains(screening.movie)
                }.toSet()
        } else
            filteredMoviesScreenings = allScreenings

        val selectedCinemas = CinemaActivity.selectedCinemas
        if (selectedCinemas.isNotEmpty()) {
            filteredCinemaScreenings =
                allScreenings.filter { screening ->
                    selectedCinemas.contains(screening.cinema)
                }.toSet()
        } else
            filteredCinemaScreenings = allScreenings

        filteredScreenings =
            filteredMoviesScreenings.intersect(filteredCinemaScreenings).toSet()

        createButtons(findViewById(R.id.gl))
    }

    private fun toast(message: String) {
        println(message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}