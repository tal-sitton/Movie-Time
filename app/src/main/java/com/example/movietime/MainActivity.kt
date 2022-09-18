package com.example.movietime

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import java.time.LocalDateTime


class MainActivity : MyTemplateActivity() {

    companion object {
        var allScreenings: List<Screening> = listOf()
        var filteredCinemaScreenings: Set<Screening> = setOf()
        var filteredMoviesScreenings: List<Screening> = listOf()
        var filteredDateScreenings: Set<Screening> = setOf()
        var filteredScreenings: List<Screening> = listOf()
        var prevFilteredScreenings: List<Screening> = listOf()
        var selectedScreeningTypes: MutableSet<String> = mutableSetOf()
        var filteredTypeScreenings: Set<Screening> = setOf()

        fun filter(fromMain: Boolean = false): Boolean {
            val selectedMovies = MovieActivity.selectedMovies
            filteredMoviesScreenings = if (selectedMovies.isNotEmpty()) {
                allScreenings.filter { screening ->
                    selectedMovies.contains(screening.movie)
                }
            } else
                allScreenings

            val selectedCinemas = CinemaActivity.selectedCinemas
            if (selectedCinemas.isNotEmpty()) {
                filteredCinemaScreenings =
                    allScreenings.filter { screening ->
                        selectedCinemas.contains(screening.cinema)
                    }.toSet()
            } else
                filteredCinemaScreenings = allScreenings.toSet()

            filteredDateScreenings = allScreenings.filter { screening ->
                DateActivity.checkScreening(screening)
            }.toSet()

            val newFilteredScreenings =
                filteredMoviesScreenings.intersect(filteredCinemaScreenings).intersect(
                    filteredDateScreenings
                ).intersect(filteredTypeScreenings).toList()

            if (newFilteredScreenings != filteredScreenings) {
                filteredScreenings = newFilteredScreenings
            }
            if (fromMain && filteredScreenings != prevFilteredScreenings) {
                prevFilteredScreenings = newFilteredScreenings
                return true
            }
            return false
        }

        fun resetToDefault() {
            filteredCinemaScreenings = allScreenings.toSet()
            filteredMoviesScreenings = allScreenings
            filteredTypeScreenings = allScreenings.toSet()
            MovieActivity.selectedMovies.clear()
            CinemaActivity.selectedCinemas.clear()
            DateActivity.default()

            filteredDateScreenings = allScreenings.filter { screening ->
                DateActivity.checkScreening(screening)
            }.toSet()

            if (filteredDateScreenings.isEmpty()) {
                DateActivity.selectedDays.clear()
                DateActivity.selectedDays.add(LocalDateTime.now().plusDays(1).dayOfMonth)
                DateActivity.restarted = false
                DateActivity.selectedDatStr = "מחר"
                filteredDateScreenings = allScreenings.filter { screening ->
                    DateActivity.checkScreening(screening)
                }.toSet()
            }

            filteredScreenings = filteredDateScreenings.toList()
            prevFilteredScreenings = filteredScreenings
        }
    }

    private var dateButton: TextView? = null

    private lateinit var scrl: ScrollView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scrl = findViewById(R.id.scrl)
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
        filteredScreenings = filteredScreenings.sortedBy { it.dateTime }
        grid.removeAllViewsInLayout()
        var i = 1
        val notFound: TextView = findViewById(R.id.noMovieFound)
        if (filteredScreenings.isEmpty()) {
            notFound.visibility = TextView.VISIBLE
            return
        } else
            notFound.visibility = TextView.INVISIBLE

        var prevDay = filteredScreenings.elementAt(0).dateTime.dayOfMonth
        if (DateActivity.selectedDays.size > 1)
            genDateTitle(prevDay, grid)

        for (screening in filteredScreenings) {
            if (prevDay != screening.dateTime.dayOfMonth) {
                genDateTitle(screening.dateTime.dayOfMonth, grid)
                prevDay = screening.dateTime.dayOfMonth
            }
            val button = screening.createButton(this)
            button.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(screening.url))
                startActivity(browserIntent)
            }
            if (i % 3 != 0) {
                val params = GridLayout.LayoutParams()
                params.setMargins(0, 0, 8, 15)
                button.layoutParams = params
            }
            grid.addView(button)
            i++
        }
    }

    private fun genDateTitle(day: Int, grid: GridLayout) {
        val dayTitle = TextView(this)
        dayTitle.text = DateActivity.genTextForSelected(day, false)
        dayTitle.textSize = 20f
        dayTitle.rotationY = 180f
        dayTitle.gravity = Gravity.CENTER_HORIZONTAL
        dayTitle.setTypeface(null, Typeface.BOLD)
        while (grid.childCount % 3 != 0) {
            grid.addView(Space(this))
        }
        grid.addView(dayTitle)
        grid.addView(Space(this))
        grid.addView(Space(this))
    }

    override fun onRestart() {
        super.onRestart()
        scrl.scrollTo(0, 0)
        dateButton?.text = DateActivity.selectedDatStr
        if (filter(true))
            createButtons(findViewById(R.id.gl))
    }

    private var backPressedTime: Long = 0

    override val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                finishAffinity()
            } else {
                Toast.makeText(baseContext, "לחץ שנית בכדי לסגור את האפליקציה", Toast.LENGTH_SHORT)
                    .show()
                backPressedTime = System.currentTimeMillis()
            }
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
            filteredTypeScreenings = allScreenings.toSet()

        onRestart()
    }
}
