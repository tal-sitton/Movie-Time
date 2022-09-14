package com.example.firstkotlinapp

import android.content.Context
import android.location.Location
import android.widget.Toast
import org.json.JSONObject
import java.net.URL
import kotlin.math.min

class Utils {
    companion object {

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
                    val cj = min(
                        1 + min(costs[j], costs[j - 1]),
                        if (str1[i - 1] == str2[j - 1]) nw else nw + 1
                    )
                    nw = costs[j]
                    costs[j] = cj
                }
            }
            return costs[str2.length]
        }

        fun measureTime(method: () -> Unit) {
            val startTime = System.currentTimeMillis()
            method()
            val endTime = System.currentTimeMillis()
            println(method.toString() + (endTime - startTime))
        }

        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun calcDistance(myLocation: Location, targetCinemas: MutableList<CinemaActivity.Cinema>) {
            val cinemas = generateCinemasToQuery(targetCinemas)
            val baseURL = "https://router.project-osrm.org/table/v1/driving/"
            val currentCoords = "${myLocation.longitude},${myLocation.latitude}"

            val strUrl = "$baseURL$currentCoords;$cinemas?sources=0&annotations=distance"
            println("STR URL: $strUrl")
            val url = URL(strUrl)
            val json = JSONObject(url.readText())
            val dist = json.getJSONArray("distances").getJSONArray(0)
            for (i in 0 until dist.length() - 1) {
                targetCinemas[i].distance = dist.getInt(i + 1)
            }

        }

        private fun generateCinemasToQuery(targetCinemas: MutableList<CinemaActivity.Cinema>): String {
            var url = ""
            targetCinemas.forEach { cinema ->
                url += "${cinema.location.longitude},${cinema.location.latitude};"
            }
            url = url.dropLast(1)
            println("URL: $url")
            return url
        }

    }
}
