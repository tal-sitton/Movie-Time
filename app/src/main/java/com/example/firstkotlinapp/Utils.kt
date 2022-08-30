package com.example.firstkotlinapp

import android.content.Context
import android.content.Intent
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.util.concurrent.Executors

class Utils {
    companion object {

        fun loadJSONFromAsset(context: Context): JSONObject? {
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
            var result = ""
            Executors.newSingleThreadExecutor().execute {
                println("getting results...")
                result = url.readText()
                writeToFile(result, context)
                println(result)
                val intent: Intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(intent)
            }
        }

        fun writeToFile(data: String, context: Context) {
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
