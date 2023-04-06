package com.example.movietime

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min


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
        const val STARTING_ROWS = 50
        const val LOADING_ROWS_CHUNK = 50
        const val SCREENING_PER_ROW = 3
        var endRow = STARTING_ROWS

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
        endRow = STARTING_ROWS
        scrl = findViewById(R.id.scrl)
        scrl.smoothScrollTo(0, 0)

        scrl.setOnScrollChangeListener(onScroll)

        ViewCompat.setNestedScrollingEnabled(scrl, false);
        ViewCompat.setNestedScrollingEnabled(findViewById(R.id.gl), false);

        JSONUtils.jsonToList(this)
        resetToDefault()

        setupTopActivityButtons()

        createButtons(findViewById(R.id.gl))
    }


    private fun isVisible(view: View): Boolean {
        val screen = Rect(
            0,
            0,
            Resources.getSystem().displayMetrics.widthPixels,
            Resources.getSystem().displayMetrics.heightPixels
        )
        val actualPosition = Rect()
        view.getGlobalVisibleRect(actualPosition)
        return actualPosition.intersect(screen)
    }

    var lastScreeningY = 0f

    private val onScroll =
        View.OnScrollChangeListener { _, _, scrollY, _, _ ->
            GlobalScope.launch(Dispatchers.Main) {
                println(scrollY)
                println(lastScreeningY)
                if (scrollY + 300 > lastScreeningY) {
                    onGridScroll()
                }
            }

        }

    private suspend fun onGridScroll() {
        val grid = findViewById<GridLayout>(R.id.gl)
        val abovePosition = ((endRow - (LOADING_ROWS_CHUNK * 1.5)) * SCREENING_PER_ROW).toInt()

        val aboveScreening = grid.getChildAt(abovePosition) as? TextView
        val lastScreening =
            grid.getChildAt((endRow - 3) * SCREENING_PER_ROW) as? TextView

        lastScreeningY = lastScreening?.y ?: 0f

        if (lastScreening != null && isVisible(lastScreening)) {
            endRow =
                min(
                    endRow + LOADING_ROWS_CHUNK,
                    (filteredScreenings.size - 1) / SCREENING_PER_ROW
                )
            createButtons(grid)
        } else {
            if (aboveScreening != null && isVisible(aboveScreening)) {
                endRow = max(endRow - LOADING_ROWS_CHUNK, STARTING_ROWS)
                createButtons(grid)
            }
        }
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

        val screenings = filteredScreenings.subList(
            0,
            minOf(
                endRow * SCREENING_PER_ROW,
                filteredScreenings.size
            )
        )

        for (screening in screenings) {
            if (prevDay != screening.dateTime.dayOfMonth) {
                genDateTitle(screening.dateTime.dayOfMonth, grid)
                prevDay = screening.dateTime.dayOfMonth
            }

            val button = screening.createButton(this)
            button.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(screening.url))
                startActivity(browserIntent)
            }
            button.minHeight = resources.getDimensionPixelSize(R.dimen.screening_height)
            button.width = resources.getDimensionPixelSize(R.dimen.screening_width)

            val metrics = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                true -> DisplayMetrics().also { display?.getRealMetrics(it) }
                false -> DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }
            }
            button.width = metrics.widthPixels / 3 - 8 * 3
            if (i % SCREENING_PER_ROW != 0) {
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
        endRow = STARTING_ROWS
        dateButton?.text = DateActivity.selectedDatStr
        if (filter(true))
            createButtons(findViewById(R.id.gl))
        scrl.scrollTo(0, 0)
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
