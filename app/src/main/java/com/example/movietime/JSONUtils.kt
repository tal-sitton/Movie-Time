package com.example.movietime

import android.content.Context
import android.content.Intent
import android.location.Location
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class JSONUtils {

    companion object {

        fun jsonToList(context: Context) {
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

            val json: JSONObject? = loadJSONFromFile(context)
            print(json)
            val tmpAllScreenings: MutableList<Screening> = mutableListOf()
            if (json != null) {
                val movies = json.getJSONArray("Movies")
                val allMovies: MutableMap<String, Movie> = mutableMapOf()
                for (i in 0 until movies.length()) {
                    val movieInfo = movies.getJSONObject(i)
                    val rating: String = try {
                        movieInfo.getDouble("rating").toString()
                    } catch (_: ClassCastException) {
                        movieInfo.getInt("rating").toString()
                    } catch (_: Exception) {
                        "N/A"
                    }
                    val movie = Movie(
                        movieInfo.getString("name"),
                        movieInfo.getString("description"),
                        rating,
                        movieInfo.getString("image_url")
                    )
                    allMovies[movie.title] = movie
                }
                MainActivity.allMovies = allMovies

                val screenings = json.getJSONArray("Screenings")
                for (i in 0 until screenings.length()) {
                    val screeningInfo = screenings.getJSONObject(i)
                    val title = screeningInfo.getString("title")
                    val coords = screeningInfo.getJSONArray("coords")

                    val screening =
                        Screening(
                            title,
                            screeningInfo.getString("date"),
                            screeningInfo.getString("time"),
                            screeningInfo.getString("location"),
                            screeningInfo.getString("district"),
                            screeningInfo.getString("cinema"),
                            screeningInfo.getString("type"),
                            screeningInfo.getString("link"),
                            Location(title).apply {
                                latitude = coords.getDouble(0)
                                longitude = coords.getDouble(1)
                            },
                            screeningInfo.getBoolean("dubbed")
                        )

                    if (screening.dateTime.isBefore(LocalDateTime.now())) {
                        continue
                    }

                    tmpAllScreenings.add(screening)
                }
            } else {
                context.startActivity(Intent(context, LoadingActivity::class.java))
            }
            MainActivity.allScreenings = tmpAllScreenings
        }

        fun loadJSONFromFile(context: Context): JSONObject? {
            val json: JSONObject? = try {
                val stream: InputStream = context.openFileInput("Movies.json")
                val size: Int = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                stream.close()
                val str = String(buffer, Charsets.UTF_8)
                JSONObject(str)
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
            return json
        }

        fun updateJSON(context: Context) {
            println("updateJSON...")
            val url =
                URL("https://raw.githubusercontent.com/tal-sitton/Movie-Time-Server/master/movies.json")
            Executors.newSingleThreadExecutor().execute {
                println("getting results...")
                try {
                    val result = url.readText()
                    writeToFile(result, context)
                    context.startActivity(Intent(context, MainActivity::class.java))
                } catch (ex: UnknownHostException) {
                    ex.printStackTrace()
                    (context as AppCompatActivity).runOnUiThread {
                        context.findViewById<ProgressBar>(R.id.progressBar).visibility =
                            ProgressBar.INVISIBLE
                        context.findViewById<TextView>(R.id.error).visibility = TextView.VISIBLE
                        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        private fun writeToFile(data: String, context: Context) {
            try {
                val outputStreamWriter =
                    OutputStreamWriter(context.openFileOutput("Movies.json", Context.MODE_PRIVATE))
                outputStreamWriter.write(data)
                outputStreamWriter.close()
            } catch (e: IOException) {
                println("Exception: File write failed: $e")
            }
        }
    }
}