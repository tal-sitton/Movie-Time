package com.example.movietime

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class MainActivity : MyTemplateActivity() {

    companion object {
        var allMovies: Map<String, Movie> = mapOf()
        var allScreenings: List<Screening> = listOf()
        var filteredCinemaScreenings: Set<Screening> = setOf()
        var filteredMoviesScreenings: List<Screening> = listOf()
        var filteredDateScreenings: Set<Screening> = setOf()
        var allowDubbed: Boolean = true
        var filteredDubScreenings: Set<Screening> = setOf()
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

            filteredDubScreenings = if (!allowDubbed) {
                allScreenings.filter { screening ->
                    !screening.dubbed
                }.toSet()
            } else
                allScreenings.toSet()

            val newFilteredScreenings =
                filteredMoviesScreenings.intersect(filteredCinemaScreenings).intersect(
                    filteredDateScreenings
                ).intersect(filteredTypeScreenings).intersect(filteredDubScreenings).toList()

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

            filteredDubScreenings = if (!allowDubbed) {
                allScreenings.filter { screening ->
                    !screening.dubbed
                }.toSet()
            } else
                allScreenings.toSet()

            filteredScreenings = filteredDateScreenings.intersect(filteredDubScreenings).toList()
            prevFilteredScreenings = filteredScreenings
        }
    }

    private var dateButton: TextView? = null

    private lateinit var settings: SharedPreferences

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
        preSetupScreeningPopup()

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

    private fun preSetupScreeningPopup() {
        val popup = findViewById<ConstraintLayout>(R.id.infoPopup)
        popup.visibility = View.GONE
        popup.setOnClickListener {
            closeScreening()
        }
    }

    private fun createButtons(recycler: RecyclerView) {
        filteredScreenings = filteredScreenings.sortedBy { it.dateTime }
        recycler.adapter = RecyclerViewAdapter(this, listOf())

        val notFound: TextView = findViewById(R.id.noMovieFound)
        val notFoundDubbed: TextView = findViewById(R.id.noMovieFoundDubbed)
        notFound.visibility = TextView.INVISIBLE
        notFoundDubbed.visibility = TextView.INVISIBLE
        if (filteredScreenings.isEmpty()) {
            if (allowDubbed)
                notFound.visibility = TextView.VISIBLE
            else
                notFoundDubbed.visibility = TextView.VISIBLE

            return
        }

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
        closeScreening()
        dateButton?.text = DateActivity.selectedDatStr
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        allowDubbed = settings.getBoolean("allowDubbed", true)
        if (filter(true))
            createButtons(recycler)

        Handler(Looper.getMainLooper()).postDelayed({ recycler.scrollToPosition(0) }, 1)
    }

    private var backPressedTime: Long = 0

    override val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val popUp = findViewById<ConstraintLayout>(R.id.infoPopup)
            if (popUp.visibility == View.VISIBLE) {
                return closeScreening()
            }
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                finishAffinity()
            } else {
                Toast.makeText(baseContext, "לחץ שנית בכדי לסגור את האפליקציה", Toast.LENGTH_SHORT)
                    .show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    fun screeningTypeFilter(view: ModifiedChip) {
        if (view.checked)
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

    private fun setupScreeningPopup(screening: Screening) {
        val movie: Movie = allMovies[screening.movie]!!
        findViewById<TextView>(R.id.movieTitle).text = movie.title

        val plot = findViewById<TextView>(R.id.plot)
        plot.text = movie.description
        plot.movementMethod = ScrollingMovementMethod()
        plot.scrollTo(0, 0)

        findViewById<TextView>(R.id.movieRating).text = movie.rating
        findViewById<TextView>(R.id.screeningHour).text = "שעה : ${screening.timeFormatted}"
        findViewById<TextView>(R.id.screeningLocation).text = "קולנוע : ${screening.cinema}"
        if (screening.dubbed)
            findViewById<TextView>(R.id.screeningDubbed).visibility = TextView.VISIBLE
        else
            findViewById<TextView>(R.id.screeningDubbed).visibility = TextView.GONE

        findViewById<TextView>(R.id.order).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(screening.url))
            startActivity(browserIntent)
        }

        CoroutineScope(Dispatchers.Default).launch {
            movie.showPoster(findViewById(R.id.poster), this@MainActivity)
        }
    }

    fun openScreening(screening: Screening) {
        setupScreeningPopup(screening)
        val popUp = findViewById<ConstraintLayout>(R.id.infoPopup)
        popUp.visibility = ConstraintLayout.VISIBLE
    }

    fun closeScreening() {
        val popUp = findViewById<ConstraintLayout>(R.id.infoPopup)
        popUp.visibility = ConstraintLayout.GONE

        findViewById<TextView>(R.id.order).setOnClickListener {
            // disable click to prevent mistakes
        }

        findViewById<ImageView>(R.id.poster).setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.image_placeholder
            )
        )
    }
}
