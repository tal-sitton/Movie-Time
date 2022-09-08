package com.example.firstkotlinapp

import android.content.Context
import android.content.Intent
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
import java.util.concurrent.Executors

class Utils {
    companion object {

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

        fun advanceStringListContains(list: List<String>, str: String): Boolean {
            for (s in list) {
                if (s == str || (s[0] == str[0] && editDistance(s, str) < 3))
                    return true
            }
            return false
        }

        private fun editDistance(str1: String, str2: String): Int {
            // i == 0
            // i == 0
            val costs = IntArray(str2.length + 1)
            for (j in costs.indices) costs[j] = j
            for (i in 1..str1.length) {
                // j == 0; nw = lev(i - 1, j)
                costs[0] = i
                var nw = i - 1
                for (j in 1..str2.length) {
                    val cj = Math.min(
                        1 + Math.min(costs[j], costs[j - 1]),
                        if (str1[i - 1] == str2[j - 1]) nw else nw + 1
                    )
                    nw = costs[j]
                    costs[j] = cj
                }
            }
            return costs[str2.length]
        }

    }
}
