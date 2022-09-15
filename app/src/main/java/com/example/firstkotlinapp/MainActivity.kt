package com.example.firstkotlinapp

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*


class MainActivity : MyTemplateActivity() {

    companion object {
        var allScreenings: Set<Screening> = setOf()
        var filteredCinemaScreenings: Set<Screening> = setOf()
        var filteredMoviesScreenings: Set<Screening> = setOf()
        var filteredDateScreenings: Set<Screening> = setOf()
        var filteredScreenings: Set<Screening> = setOf()
        var selectedScreeningTypes: MutableSet<String> = mutableSetOf()
        var filteredTypeScreenings: Set<Screening> = setOf()

        fun filter(): Boolean {
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

            filteredDateScreenings = allScreenings.filter { screening ->
                DateActivity.checkScreening(screening)
            }.toSet()

            val newFilteredScreenings =
                filteredMoviesScreenings.intersect(filteredCinemaScreenings).intersect(
                    filteredDateScreenings
                ).intersect(filteredTypeScreenings).toSet()

            if (newFilteredScreenings != filteredScreenings) {
                filteredScreenings = newFilteredScreenings
                return true
            }
            return false
        }

        fun resetToDefault() {
            filteredCinemaScreenings = allScreenings
            filteredMoviesScreenings = allScreenings
            filteredTypeScreenings = allScreenings
            MovieActivity.selectedMovies.clear()
            CinemaActivity.selectedCinemas.clear()
            DateActivity.default()

            filteredDateScreenings = allScreenings.filter { screening ->
                DateActivity.checkScreening(screening)
            }.toSet()

            filteredScreenings = filteredDateScreenings
        }
    }

    private var dateButton: TextView? = null

    private lateinit var scrl: ScrollView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.showToast(this, "onCreate")
        val scrl: ScrollView = findViewById(R.id.scrl)
        scrl.smoothScrollTo(0, 0)

        JSONUtils.jsonToList(this)
        resetToDefault()

        setupTopActivityButtons()

        createButtons(findViewById(R.id.gl))
    }

    private fun setupTopActivityButtons() {
        val movieButton: TextView = findViewById(R.id.movieButton)
        dateButton = findViewById(R.id.dateButton)
        val cinemaButton: TextView = findViewById(R.id.cinemaButton)

        val intent = Intent()
        intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        movieButton.setOnClickListener {
            intent.setClass(this, MovieActivity::class.java)
            startActivity(intent)
        }

        dateButton?.setOnClickListener {
            intent.setClass(this, DateActivity::class.java)
            startActivity(intent)
        }
        dateButton?.text = DateActivity.selectedDatStr

        cinemaButton.setOnClickListener {
            intent.setClass(this, CinemaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createButtons(grid: GridLayout) {
        grid.removeAllViewsInLayout()
        var i = 1
        val notFound: TextView = findViewById(R.id.noMovieFound)
        if (filteredScreenings.isEmpty())
            notFound.visibility = TextView.VISIBLE
        else
            notFound.visibility = TextView.INVISIBLE

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
        scrl.scrollTo(0, 0)
        dateButton?.text = DateActivity.selectedDatStr
        if (filter())
            createButtons(findViewById(R.id.gl))
    }

    private var backPressedTime: Long = 0
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            this.finishAffinity()
        } else {
            Toast.makeText(baseContext, "Press the back button again to exit", Toast.LENGTH_SHORT)
                .show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    fun screeningTypeFilter(view: View) {
        view as CheckBox
        if (view.isChecked)
            selectedScreeningTypes.add(view.text.toString())
        else
            selectedScreeningTypes.remove(view.text.toString())

        if (selectedScreeningTypes.isNotEmpty()) {
            filteredTypeScreenings =
                allScreenings.filter { screening ->
                    selectedScreeningTypes.contains(screening.type)
                }.toSet()
        } else
            filteredTypeScreenings = allScreenings

        onRestart()
    }
}
