package com.example.firstkotlinapp

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CinemaActivity : AppCompatActivity() {
    /*
    TODO organize cinemas, or maybe the sql will come in a certain order?
     */

    companion object {
        val selectedCinemas: MutableList<String> = mutableListOf("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter)
        setupButton()

        val cinemas: List<String> = getCinemas()
        val ll: LinearLayout = findViewById(R.id.ll)
        createMoviesButtons(cinemas, ll)

    }

    private fun createMoviesButtons(cinemas: List<String>, ll: LinearLayout) {
        for (cinema in cinemas) {
            val button = Button(this)
            button.text = cinema
            if (selectedCinemas.contains(cinema)) {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.clicked_button)
            } else
                button.setBackgroundResource(R.drawable.unclicked_button)
            button.setOnClickListener {
                if (button.isSelected) {
                    button.setBackgroundResource(R.drawable.unclicked_button)
                    button.isSelected = false
                    selectedCinemas.remove(cinema)
                } else {
                    button.setBackgroundResource(R.drawable.clicked_button)
                    button.isSelected = true
                    selectedCinemas.add(cinema)
                }
            }
            ll.addView(button)
            val spacer = Space(this)
            spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
            ll.addView(spacer)
        }
    }

    private fun setupButton() {
        val mainButton: Button = findViewById(R.id.cinemaButton)
        mainButton.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.purple_700
            )
        )
        mainButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun getCinemas(): List<String> {
        val cinemas: MutableList<String> = mutableListOf()
        for (screening in MainActivity.allScreenings) {
            if (!cinemas.contains(screening.cinema)) {
                cinemas.add(screening.cinema)
            }
        }
        return cinemas
    }
}