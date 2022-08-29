package com.example.firstkotlinapp

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class Utils {
    companion object {

        fun loadJSONFromAsset(context: Context): JSONObject? {
            val json: String = try {
                val stream: InputStream = context.assets.open("Movies.json")
                val size: Int = stream.available()
                val buffer = ByteArray(size)
                stream.read(buffer)
                stream.close()
                String(buffer, Charsets.UTF_8)
            } catch (ex: IOException) {
                ex.printStackTrace()
                return null
            }
            return JSONObject(json)
        }

    }
}
