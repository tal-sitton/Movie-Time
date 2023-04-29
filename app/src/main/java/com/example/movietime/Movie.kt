package com.example.movietime;

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.example.movietime.Utils.Companion.downloadImage
import com.example.movietime.Utils.Companion.dpToPx
import java.net.UnknownHostException


class Movie(
    val title: String,
    val description: String,
    val rating: String,
    private val posterUrl: String,
) {
    private var poster: RoundedBitmapDrawable? = null

    fun generatePoster(view: ImageView, context: Context) {
        var image = downloadImage(posterUrl)
        image = Bitmap.createScaledBitmap(image, view.width, view.height, false)
        poster = RoundedBitmapDrawableFactory.create(context.resources, image)
        val roundPx = dpToPx(context, 8)
        poster?.cornerRadius = roundPx
    }

    fun showPoster(view: ImageView, context: Activity) {
        val progressBar = context.findViewById<ProgressBar>(R.id.loadingPoster)

        context.runOnUiThread {
            view.setImageDrawable(poster)
            progressBar?.visibility = ProgressBar.VISIBLE
        }

        if (poster == null) {
            try {
                generatePoster(view, context)
            } catch (_: UnknownHostException) {
            } catch (e: Exception) {
                println("ERROR: Could not load poster for $title")
                println(e)
            }
        }
        context.runOnUiThread {
            view.setImageDrawable(poster)
            progressBar?.visibility = ProgressBar.INVISIBLE
        }

        if (poster == null) {
            context.runOnUiThread {
                view.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.image_placeholder
                    )
                )
            }
        }
    }
}