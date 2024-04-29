package com.example.movietime

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.*

class MovieActivity : MyTemplateActivity() {

    companion object {
        val selectedMovies: MutableList<String> = mutableListOf("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter_movie)
        setupTopButtons()

        val movies: List<String> = getMovies()
        val ll: LinearLayout = findViewById(R.id.ll)
        createMoviesButtons(movies, ll)

        val search = findViewById<EditText>(R.id.search)
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                val availableMovies = filterMovies(movies)
                createMoviesButtons(availableMovies, ll)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterMovies(movies: List<String>): List<String> {
        val search = findViewById<EditText>(R.id.search)
        return movies.filter { it.contains(search.text.toString()) }
    }

    private fun createMoviesButtons(movies: List<String>, ll: LinearLayout) {
        ll.removeAllViewsInLayout()
        val showMovies = movies.sortedWith { o1, o2 -> Utils.comparePreferHebrew(o1, o2) }
            .sortedBy { !selectedMovies.contains(it) }

        for (movie in showMovies) {
            val button = Button(this)
            button.text = movie
            if (selectedMovies.contains(movie)) {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.clicked_button)
            } else
                button.setBackgroundResource(R.drawable.unclicked_button)
            button.setOnClickListener {
                if (button.isSelected) {
                    button.setBackgroundResource(R.drawable.unclicked_button)
                    button.isSelected = false
                    selectedMovies.remove(movie)
                } else {
                    button.setBackgroundResource(R.drawable.clicked_button)
                    button.isSelected = true
                    selectedMovies.add(movie)
                }
                createMoviesButtons(showMovies, ll)
            }
            ll.addView(button)
            val spacer = Space(this)
            spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
            ll.addView(spacer)
        }
    }

    private fun setupTopButtons() {
        val movieButton: TextView = findViewById(R.id.movieButton)
        movieButton.setBackgroundResource(R.drawable.top_buttons_clicked)
        movieButton.setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }
        movieButton.text = "אישור"

        val dateButton: TextView = findViewById(R.id.dateButton)
        val cinemaButton: TextView = findViewById(R.id.cinemaButton)

        dateButton.text = DateActivity.selectedDatStr

        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        dateButton.setOnClickListener {
            intent.setClass(this, DateActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
        cinemaButton.setOnClickListener {
            intent.setClass(this, CinemaActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
    }

    private fun getMovies(): List<String> {
        val movies: MutableList<String> = mutableListOf()
        for (screening in MainActivity.filteredCinemaScreenings.intersect(MainActivity.filteredDateScreenings)
            .intersect(MainActivity.filteredDubScreenings)) {
            if (!Utils.advanceStringListContains(movies, screening.movie)) {
                movies.add(screening.movie)
            }
        }
        movies.sort()
        return movies
    }

}
