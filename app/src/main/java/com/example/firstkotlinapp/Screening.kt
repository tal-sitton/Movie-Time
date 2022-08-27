package com.example.firstkotlinapp

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Screening(
    val movie: String,
    val date: String,
    val time: String,
    val city: String,
    val theater: String,
    val type: String,
    val url: String
) {

    val cinema: String = "$city $theater"
    var dateTime: LocalDateTime = LocalDateTime.MIN
    var dateTimeFormatted: String? = null

    init {
        println("Screening: $movie, $date, $time, $city, $theater, $type, $url")
        val day = date.split("-")[0].toInt()
        val month = date.split("-")[1].toInt() - 1
        val year = date.split("-")[2].toInt()
        val hour = time.split(":")[0].toInt()
        val minute = time.split(":")[1].toInt()
        dateTime = LocalDateTime.of(year, month, day, hour, minute)
        dateTimeFormatted =
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm - dd.MM"))
    }


    fun createButton(context: Context): TextView {
        val button = TextView(context)
        button.width = 330
        button.minHeight = 442
        button.textAlignment = ViewGroup.TEXT_ALIGNMENT_CENTER

        button.setBackgroundResource(R.drawable.movie_button)
        button.setTextColor(context.resources.getColor(R.color.white, context.theme))

        val text: Spannable =
            SpannableString("$movie ($type)\n\n$city\n$theater\n$dateTimeFormatted\n")
        text.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            "$movie ($type)".length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        text.setSpan(
            RelativeSizeSpan(1.15f),
            0,
            "$movie ($type)".length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        text.setSpan(
            StyleSpan(Typeface.BOLD),
            "$movie ($type)".length + 2,
            "$movie ($type)".length + 1 + "$city\n\n$theater".length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        button.text = text

        println(text)
        println("BUTTON HEIGHTTT: ${button.height}")
        return button
    }

}