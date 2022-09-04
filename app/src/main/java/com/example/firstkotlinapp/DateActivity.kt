package com.example.firstkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDateTime

class DateActivity : AppCompatActivity() {
    /*
    TODO THIS ONE IS BIT DIFFICULT
     */
    companion object {
        var selectedDay: Int = LocalDateTime.now().dayOfMonth
        var selectedStartHour: Int = LocalDateTime.now().hour
        var selectedEndHour = 24
        var restarted = false

        fun checkScreening(screening: Screening): Boolean {
            val date = screening.dateTime
            return date.dayOfMonth == selectedDay && date.hour >= selectedStartHour && date.hour <= selectedEndHour
        }

        fun default() {
            selectedDay = LocalDateTime.now().dayOfMonth
            selectedStartHour = LocalDateTime.now().hour
            selectedEndHour = 24
            restarted = true
        }

        private var pressedDayID: Int = 0
        private var pressedHourID: Int = 0
    }

    private var pressedDay: Button? = null
    private var pressedHour: Button? = null
    private var grid: GridLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grid = findViewById(R.id.grid)
        setContentView(R.layout.actvity_filter_date)
        setupTopButtons()

        if (restarted) {
            pressedDayID = R.id.todayButton
            pressedHourID = R.id.allDayButton
            restarted = false
        }
        if (pressedDay == null) {
            pressedDay = findViewById(pressedDayID)
            pressedHour = findViewById(pressedHourID)
        }

        pressedDay?.setBackgroundColor(this.getColor(R.color.purple_500))
        pressedHour?.setBackgroundColor(this.getColor(R.color.purple_500))

        setupDateButtons()
    }

    private fun setupDateButtons() {
        val now = LocalDateTime.now()

        val today = findViewById<Button>(R.id.todayButton)
        today.setOnClickListener { dayClickListener(today, now) }

        val tomorrow = findViewById<Button>(R.id.tomorrowButton)
        tomorrow.setOnClickListener { dayClickListener(tomorrow, now.plusDays(1)) }

        val twoDays = findViewById<Button>(R.id.twoDaysButton)
        twoDays.text = genText(now.plusDays(2))
        twoDays.setOnClickListener { dayClickListener(twoDays, now.plusDays(2)) }

        val threeDays = findViewById<Button>(R.id.threeDaysButton)
        threeDays.text = genText(now.plusDays(3))
        threeDays.setOnClickListener { dayClickListener(threeDays, now.plusDays(3)) }

        val fourDays = findViewById<Button>(R.id.fourDaysButton)
        fourDays.text = genText(now.plusDays(4))
        fourDays.setOnClickListener { dayClickListener(fourDays, now.plusDays(4)) }
    }

    private fun dayClickListener(button: Button, dateTime: LocalDateTime) {
        pressedDay?.setBackgroundColor(this.getColor(R.color.purple_200))
        pressedDay = button
        pressedDayID = button.id
        pressedDay?.setBackgroundColor(this.getColor(R.color.purple_500))
        selectedDay = dateTime.dayOfMonth

    }

    private fun genText(dateTime: LocalDateTime): String {
        return "יום " + when (dateTime.dayOfWeek) {
            DayOfWeek.SUNDAY -> "ראשון"
            DayOfWeek.MONDAY -> "שני"
            DayOfWeek.TUESDAY -> "שלישי"
            DayOfWeek.WEDNESDAY -> "רביעי"
            DayOfWeek.THURSDAY -> "חמישי"
            DayOfWeek.FRIDAY -> "שישי"
            DayOfWeek.SATURDAY -> "שבת"
        } + " " + dateTime.dayOfMonth + "/" + dateTime.monthValue
    }

    private fun setupTopButtons() {
        val mainButton: Button = findViewById(R.id.dateButton)
        mainButton.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.purple_700
            )
        )
        mainButton.setOnClickListener {
            onBackPressed()
        }
        val cinemaButton: Button = findViewById(R.id.cinemaButton)
        val movieButton: Button = findViewById(R.id.movieButton)

        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        cinemaButton.setOnClickListener {
            intent.setClass(this, CinemaActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
        movieButton.setOnClickListener {
            intent.setClass(this, MovieActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(0, 0)
    }

}
