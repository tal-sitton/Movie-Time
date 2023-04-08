package com.example.movietime

class Title(val text: String) : Recyclable {
    override val url: String = ""

    override fun createText(): CharSequence {
        return text
    }
}
