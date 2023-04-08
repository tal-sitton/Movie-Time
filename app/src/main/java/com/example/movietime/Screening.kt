package com.example.movietime

import android.graphics.Typeface
import android.location.Location
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Screening(
    val movie: String,
    date: String,
    val time: String,
    val city: String,
    val district: String,
    val theater: String,
    val type: String,
    override val url: String,
    val location: Location,
    val dubbed: Boolean
) : Recyclable {

    val cinema: String = "$theater $city"
    var dateTime: LocalDateTime = LocalDateTime.MIN
    var timeFormatted: String
    private val dubbedText: String = if (dubbed) "- מדובב " else ""

    init {
        val day = date.split("-")[0].toInt()
        val month = date.split("-")[1].toInt()
        val year = date.split("-")[2].toInt()
        val hour = time.split(":")[0].toInt()
        val minute = time.split(":")[1].toInt()
        dateTime = LocalDateTime.of(year, month, day, hour, minute)
        timeFormatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    override fun toString(): String {
        return "Screening: $movie, $dateTime, $city, $theater, $type, $dubbed, $url"
    }


    override fun createText(): Spannable {
        val text: Spannable =
            SpannableString("$timeFormatted\n\n$movie $dubbedText($type)\n\n$city\n$theater")
        text.setSpan(
            UnderlineSpan(),
            text.indexOf(timeFormatted),
            timeFormatted.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text.setSpan(
            StyleSpan(Typeface.BOLD),
            text.indexOf(timeFormatted),
            timeFormatted.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text.setSpan(
            StyleSpan(Typeface.BOLD),
            text.indexOf(movie),
            text.indexOf(type) + type.length + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text.setSpan(
            RelativeSizeSpan(1.15f),
            text.indexOf(movie),
            text.indexOf(type) + type.length + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text.setSpan(
            StyleSpan(Typeface.BOLD),
            text.lastIndexOf(city),
            text.lastIndexOf(theater),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return text
    }

}
