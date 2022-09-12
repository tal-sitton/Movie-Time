package com.example.firstkotlinapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

class CinemaActivity : MyTemplateActivity() {

    companion object {
        val selectedCinemas: MutableList<String> = mutableListOf("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter)
        setupTopButtons()

        val cinemas: List<Cinema> = getCinemas()
        val ll: LinearLayout = findViewById(R.id.ll)
        createCinemasButtons(cinemas, ll)

    }

    private fun createCinemasButtons(cinemas: List<Cinema>, ll: LinearLayout) {
        var district = cinemas[0].district
        createDisTextView(district, ll)
        for (cinema in cinemas) {
            if (cinema.district != district) {
                district = cinema.district
                createDisTextView(district, ll)
            }
            val button = Button(this)
            button.text = cinema.name
            if (selectedCinemas.contains(cinema.name)) {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.clicked_button)
            } else
                button.setBackgroundResource(R.drawable.unclicked_button)
            button.setOnClickListener {
                if (button.isSelected) {
                    button.setBackgroundResource(R.drawable.unclicked_button)
                    button.isSelected = false
                    selectedCinemas.remove(cinema.name)
                } else {
                    button.setBackgroundResource(R.drawable.clicked_button)
                    button.isSelected = true
                    selectedCinemas.add(cinema.name)
                }
            }
            ll.addView(button)
            val spacer = Space(this)
            spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
            ll.addView(spacer)
        }
    }

    private fun createDisTextView(district: String, ll: LinearLayout) {
        val districtText = TextView(this)
        districtText.text = district
        districtText.textSize = 20.0f
        districtText.setTypeface(null, Typeface.BOLD)
        districtText.gravity = android.view.Gravity.CENTER
        ll.addView(districtText)
        val spacer = Space(this)
        spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
        ll.addView(spacer)
    }

    private fun setupTopButtons() {
        val mainButton: TextView = findViewById(R.id.cinemaButton)
        mainButton.setBackgroundResource(R.drawable.top_buttons_clicked)
        mainButton.setOnClickListener {
            onBackPressed()
        }
        val dateButton: TextView = findViewById(R.id.dateButton)
        val movieButton: TextView = findViewById(R.id.movieButton)

        dateButton.text = DateActivity.selectedDatStr

        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        dateButton.setOnClickListener {
            intent.setClass(this, DateActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
        movieButton.setOnClickListener {
            intent.setClass(this, MovieActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
    }

    private fun getCinemas(): List<Cinema> {
        val cinemas: MutableList<Cinema> = mutableListOf()
        for (screening in MainActivity.filteredMoviesScreenings.intersect(MainActivity.filteredDateScreenings)) {
            val tmpCinema = Cinema(screening.cinema, screening.district)
            if (!cinemas.any { cinema -> cinema == tmpCinema }) {
                cinemas.add(tmpCinema)
            }
        }
        return cinemas
    }

    class Cinema(val name: String, val district: String) {
        override fun equals(other: Any?): Boolean {
            return other is Cinema && other.name == name && other.district == district
        }
    }
}
