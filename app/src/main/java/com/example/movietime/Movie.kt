package com.example.movietime;

import android.app.Activity
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.example.movietime.Utils.Companion.downloadImage
import com.example.movietime.Utils.Companion.dpToPx


class Movie(
    val title: String,
    val description: String,
    val rating: String,
    private val posterUrl: String,
) {
    private var poster: RoundedBitmapDrawable? = null

    fun generatePoster(view: ImageView, context: Activity) {
        if (poster == null) {
            var image = downloadImage(posterUrl)
            image = Bitmap.createScaledBitmap(image, view.width, view.height, false)
            poster = RoundedBitmapDrawableFactory.create(context.resources, image)
            val roundPx = dpToPx(context, 8)
            poster?.cornerRadius = roundPx
        }
        context.runOnUiThread {
            view.setImageDrawable(poster)

        }
    }
}