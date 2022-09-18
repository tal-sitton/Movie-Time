package com.example.movietime

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.time.DayOfWeek
import java.time.LocalDateTime

class DateActivity : MyTemplateActivity() {

    companion object {
        var selectedDays: MutableList<Int> = mutableListOf(LocalDateTime.now().dayOfMonth)
        var selectedDatStr: String = "היום"
        val selectedHour: MutableList<Hour> = mutableListOf(Hour.ALL_DAY)
        var restarted = true

        fun checkScreening(screening: Screening): Boolean {
            val date = screening.dateTime
            return date.dayOfMonth in selectedDays && selectedHour.any { it.between(date) }
        }

        fun default() {
            selectedDays = mutableListOf(LocalDateTime.now().dayOfMonth)
            selectedHour.clear()
            selectedHour.add(Hour.ALL_DAY)
            restarted = true
            selectedDatStr = "היום"
        }

        private fun genText(dateTime: LocalDateTime, addDate: Boolean = true): String {
            var out = "יום " + when (dateTime.dayOfWeek) {
                DayOfWeek.SUNDAY -> "ראשון"
                DayOfWeek.MONDAY -> "שני"
                DayOfWeek.TUESDAY -> "שלישי"
                DayOfWeek.WEDNESDAY -> "רביעי"
                DayOfWeek.THURSDAY -> "חמישי"
                DayOfWeek.FRIDAY -> "שישי"
                DayOfWeek.SATURDAY -> "שבת"
            }
            return if (addDate) out + " " + dateTime.dayOfMonth + "/" + dateTime.monthValue else out
        }

        fun genTextForSelected(day: Int, addDate: Boolean = true): String {
            return when (day) {
                LocalDateTime.now().dayOfMonth -> "היום"
                LocalDateTime.now().plusDays(1).dayOfMonth -> "מחר"
                in 1 until LocalDateTime.now().dayOfMonth -> genText(
                    LocalDateTime.now().plusMonths(1).withDayOfMonth(day), addDate
                )
                else ->
                    genText(LocalDateTime.now().withDayOfMonth(day), addDate)
            }
        }

    }

    private lateinit var allDay: Button
    private lateinit var today: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter_date)
        setupTopButtons()
        allDay = findViewById(R.id.allDayButton)
        today = findViewById(R.id.todayButton)

        if (restarted) {
            allDay.setBackgroundColor(this.getColor(R.color.purple_500))
            today.setBackgroundColor(this.getColor(R.color.purple_500))
        }

        setupDateButtons()
        setupHourButtons()
    }

    private fun setupDateButtons() {
        val now = LocalDateTime.now()

        val today = findViewById<Button>(R.id.todayButton)
        configDayButton(today, now.dayOfMonth)

        val tomorrow = findViewById<Button>(R.id.tomorrowButton)
        configDayButton(tomorrow, now.plusDays(1).dayOfMonth)

        val twoDays = findViewById<Button>(R.id.twoDaysButton)
        twoDays.text = genText(now.plusDays(2))
        configDayButton(twoDays, now.plusDays(2).dayOfMonth)

        val threeDays = findViewById<Button>(R.id.threeDaysButton)
        threeDays.text = genText(now.plusDays(3))
        configDayButton(threeDays, now.plusDays(3).dayOfMonth)

        val fourDays = findViewById<Button>(R.id.fourDaysButton)
        fourDays.text = genText(now.plusDays(4))
        configDayButton(fourDays, now.plusDays(4).dayOfMonth)
    }

    private fun configDayButton(button: Button, day: Int) {
        if (!restarted) {
            if (selectedDays.contains(day)) {
                button.setBackgroundColor(this.getColor(R.color.purple_500))
            } else {
                button.setBackgroundColor(this.getColor(R.color.purple_200))
            }
        }
        button.setOnClickListener { dayClickListener(button, day) }
    }

    private fun dayClickListener(button: Button, day: Int) {
        val nowDay = LocalDateTime.now().dayOfMonth
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            button.setBackgroundColor(this.getColor(R.color.purple_200))
            if (selectedDays.isEmpty()) {
                selectedDays.add(nowDay)
                today.setBackgroundColor(this.getColor(R.color.purple_500))
            }
        } else {
            selectedDays.add(day)
            button.setBackgroundColor(this.getColor(R.color.purple_500))
        }
        if (selectedDays.size == 1) {
            selectedDatStr = genTextForSelected(selectedDays[0])
        } else {
            selectedDatStr = "מספר ימים"
        }
    }

    private fun setupHourButtons() {
        val allDay = findViewById<Button>(R.id.allDayButton)
        configHourButton(allDay, Hour.ALL_DAY)

        val before12 = findViewById<Button>(R.id.before12Button)
        configHourButton(before12, Hour.BEFORE_12)

        val till15 = findViewById<Button>(R.id.till15Button)
        configHourButton(till15, Hour.TILL_15)

        val till19 = findViewById<Button>(R.id.till19Button)
        configHourButton(till19, Hour.TILL_19)

        val after19 = findViewById<Button>(R.id.after19Button)
        configHourButton(after19, Hour.AFTER_19)
        restarted = false
    }

    private fun configHourButton(button: Button, hour: Hour) {
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
            onBackPressedCallback.handleOnBackPressed()
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
