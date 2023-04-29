package com.example.movietime

import android.content.Context

interface Recyclable {

    val url: String

    fun createText(): CharSequence;

    fun onClick(context: Context) {
        // Do nothing
    }
}
