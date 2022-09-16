package com.example.movietime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.time.DayOfWeek
import java.time.LocalDateTime

class DateActivity : MyTemplateActivity() {

    companion object {
        var selectedDay: Int = LocalDateTime.now().dayOfMonth
        var selectedDatStr: String = "היום"
        val selectedHour: MutableList<Hour> = mutableListOf(Hour.ALL_DAY)
        var restarted = true

        fun checkScreening(screening: Screening): Boolean {
            val date = screening.dateTime
            return date.dayOfMonth == selectedDay && selectedHour.any { it.between(date) }
        }

        fun default() {
            selectedDay = LocalDateTime.now().dayOfMonth
            selectedHour.clear()
            selectedHour.add(Hour.ALL_DAY)
            restarted = true
            selectedDatStr = "היום"
        }

        private var pressedDayID: Int = 0
    }

    private var pressedDay: Button? = null
    private lateinit var allDay: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter_date)
        setupTopButtons()
        allDay = findViewById(R.id.allDayButton)

        if (restarted) {
            pressedDayID = R.id.todayButton
            allDay.setBackgroundColor(this.getColor(R.color.purple_500))
        }
        if (pressedDay == null) {
            pressedDay = findViewById(pressedDayID)
        }

        pressedDay?.setBackgroundColor(this.getColor(R.color.purple_500))

        setupDateButtons()
        setupHourButtons()
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
        selectedDatStr = button.text.toString()
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

    private fun setupHourButtons() {
        val allDay = findViewById<Button>(R.id.allDayButton)
        configButton(allDay, Hour.ALL_DAY)

        val before12 = findViewById<Button>(R.id.before12Button)
        configButton(before12, Hour.BEFORE_12)

        val till15 = findViewById<Button>(R.id.till15Button)
        configButton(till15, Hour.TILL_15)

        val till19 = findViewById<Button>(R.id.till19Button)
        configButton(till19, Hour.TILL_19)

        val after19 = findViewById<Button>(R.id.after19Button)
        configButton(after19, Hour.AFTER_19)
        restarted = false
    }

    private fun configButton(button: Button, hour: Hour) {
        if (!restarted) {
            if (selectedHour.contains(hour)) {
                button.setBackgroundColor(this.getColor(R.color.purple_500))
            } else {
                button.setBackgroundColor(this.getColor(R.color.purple_200))
            }
        }
        button.setOnClickListener { hourClickListener(button, hour) }
    }

    private fun hourClickListener(button: Button, hour: Hour) {
        if (selectedHour.contains(hour)) {
            selectedHour.remove(hour)
            button.setBackgroundColor(this.getColor(R.color.purple_200))
            if (selectedHour.isEmpty()) {
                selectedHour.add(Hour.ALL_DAY)
                allDay.setBackgroundColor(this.getColor(R.color.purple_500))
            }
        } else {
            if (hour == Hour.ALL_DAY) {
                selectedHour.clear()
                selectedHour.add(Hour.ALL_DAY)
                allDay.setBackgroundColor(this.getColor(R.color.purple_500))
                findViewById<Button>(R.id.before12Button).setBackgroundColor(this.getColor(R.color.purple_200))
                findViewById<Button>(R.id.till15Button).setBackgroundColor(this.getColor(R.color.purple_200))
                findViewById<Button>(R.id.till19Button).setBackgroundColor(this.getColor(R.color.purple_200))
                findViewById<Button>(R.id.after19Button).setBackgroundColor(this.getColor(R.color.purple_200))
            } else {
                selectedHour.remove(Hour.ALL_DAY)
                allDay.setBackgroundColor(this.getColor(R.color.purple_200))
                selectedHour.add(hour)
                button.setBackgroundColor(this.getColor(R.color.purple_500))
            }
        }
    }

    private fun setupTopButtons() {
        val mainButton: TextView = findViewById(R.id.dateButton)
        mainButton.setBackgroundResource(R.drawable.top_buttons_clicked)
        mainButton.text = "אישור"
        mainButton.setOnClickListener {
            onBackPressed()
        }
        val cinemaButton: TextView = findViewById(R.id.cinemaButton)
        val movieButton: TextView = findViewById(R.id.movieButton)

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

    enum class Hour(val start: Int, val end: Int) {
        ALL_DAY(0, 24), BEFORE_12(0, 12), TILL_15(12, 15), TILL_19(15, 19), AFTER_19(19, 24);

        fun between(date: LocalDateTime): Boolean {
            return date.hour >= start && (date.hour < end || (date.hour == end && date.minute == 0))
        }

        override fun toString(): String {
            return "$start:00 - $end:00"
        }
    }

}
