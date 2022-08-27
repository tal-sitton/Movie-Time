package com.example.firstkotlinapp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DateActivity : AppCompatActivity() {
    /*
    TODO THIS ONE IS BIT DIFFICULT
     */
    companion object {
        val selectedMovies: MutableList<String> = mutableListOf("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter)
        setupButton()

        val movies: List<String> = moviesFromSQL()
        val ll: LinearLayout = findViewById(R.id.ll)
        createMoviesButtons(movies, ll)

    }

    private fun createMoviesButtons(movies: List<String>, ll: LinearLayout) {
        for (movie in movies) {
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
            }
            ll.addView(button)
            val spacer = Space(this)
            spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
            ll.addView(spacer)
        }
    }

    private fun setupButton() {
        val movieButton: Button = findViewById(R.id.movieButton)
        movieButton.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.purple_700
            )
        )
        movieButton.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun moviesFromSQL(): List<String> {
        //TODO: get movies from SQL
        return listOf(
            "חומוס FULL טריילר",
            "כלב מי שמנגן 2",
            "לא רעים בכלל",
            "לב של אש",
            "לומדת לעוף",
            "מאה אחוז זאב",
            "סאמרלנד",
            "סוניק 2",
            "סופר העולם החדשה",
            "עולה לראש",
            "עולם היורה: עולם חדש",
            "שיעורים בפרסית",
            "שנות-אור",
            "שניהם בקולנוע",
            "תוכנית פריצה",
            "תור: אהבה ורעם",
            "המיניונים 2: עלייתו של גרו "
        )
    }
}