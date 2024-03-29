package com.example.movietime

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.RecyclerView
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
        const val SCREENING_PER_ROW = 3

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

    private lateinit var settings: SharedPreferences
    private var allowDubbed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = getSharedPreferences("preferences", MODE_PRIVATE)
        allowDubbed = settings.getBoolean("allowDubbed", true)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, SCREENING_PER_ROW)

        JSONUtils.jsonToList(this)
        resetToDefault()

        setupTopActivityButtons()

        createButtons(recycler)

        Handler(Looper.getMainLooper()).postDelayed({ recycler.scrollToPosition(0) }, 1)
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

    private fun createButtons(recycler: RecyclerView) {
        allowDubbed = settings.getBoolean("allowDubbed", true)
        if (!allowDubbed)
            filteredScreenings = filteredScreenings.filter { !it.dubbed }.toSet().toList()

        filteredScreenings = filteredScreenings.sortedBy { it.dateTime }
        recycler.removeAllViewsInLayout()
        var i = 1
        val notFound: TextView = findViewById(R.id.noMovieFound)
        if (filteredScreenings.isEmpty()) {
            notFound.visibility = TextView.VISIBLE
            return
        } else
            notFound.visibility = TextView.INVISIBLE

        var prevDay = filteredScreenings.elementAt(0).dateTime.dayOfMonth
        val screenings: MutableList<Recyclable> = mutableListOf()

        if (DateActivity.selectedDays.size > 1) {
            addDateTitle(prevDay, screenings)
        }

        for (screening in filteredScreenings) {
            if (screening.dateTime.dayOfMonth != prevDay) {
                while (screenings.size % SCREENING_PER_ROW != 0) {
                    screenings.add(RecyclableSpace())
                }
                addDateTitle(screening.dateTime.dayOfMonth, screenings)
                prevDay = screening.dateTime.dayOfMonth
            }
            screenings.add(screening)
        }

        recycler.adapter = RecyclerViewAdapter(this, screenings)
    }

    private fun addDateTitle(day: Int, screenings: MutableList<Recyclable>) {
        screenings.add(Title(DateActivity.genTextForSelected(day, false)))
        screenings.add(RecyclableSpace())
        screenings.add(RecyclableSpace())
    }

    override fun onRestart() {
        super.onRestart()
        dateButton?.text = DateActivity.selectedDatStr
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        if (filter(true) || allowDubbed != settings.getBoolean("allowDubbed", true))
            createButtons(recycler)

        Handler(Looper.getMainLooper()).postDelayed({ recycler.scrollToPosition(0) }, 1)
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
