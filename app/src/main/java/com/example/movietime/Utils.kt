package com.example.movietime

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.TypedValue
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import org.json.JSONObject
import java.net.URL
import kotlin.math.min

class Utils {
    companion object {

        const val LOCATION_REQUEST_CODE = 23

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
            val url = URL(strUrl)
            val json = JSONObject(url.readText())
            val dist = json.getJSONArray("distances").getJSONArray(0)
            for (i in 0 until dist.length() - 1) {
                targetCinemas[i].distance = dist.getDouble(i + 1)
            }

        }

        private fun generateCinemasToQuery(targetCinemas: MutableList<CinemaActivity.Cinema>): String {
            var url = ""
            targetCinemas.forEach { cinema ->
                url += "${cinema.location.longitude},${cinema.location.latitude};"
            }
            return url.dropLast(1)
        }

        fun downloadImage(url: String): Bitmap {
            val inputStream = URL(url).openStream()
            return BitmapFactory.decodeStream(inputStream)
        }

        fun dpToPx(context: Context, dp: Int): Float {
            val r: Resources = context.resources
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            )
        }

        fun enableLocation(context: Activity, callback: (Boolean) -> Unit) {

            val locationRequest: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(false)
                    .setIntervalMillis(10000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()


            val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            val client = LocationServices.getSettingsClient(context)
            val task = client.checkLocationSettings(builder.build())

            task.addOnFailureListener(
                (context)!!
            ) { e: Exception ->
                if (e is ResolvableApiException) {
                    try {
                        showToast(context, "יש להפעיל את המיקום")
                        val resolvable: ResolvableApiException = e
                        resolvable.startResolutionForResult(
                            context,
                            LOCATION_REQUEST_CODE
                        )
                    } catch (sendEx: SendIntentException) {
                        sendEx.printStackTrace()
                        callback.invoke(false)
                    }
                }
            }

            task.addOnCanceledListener(context) { callback.invoke(false) }
            task.addOnSuccessListener(context) { callback.invoke(true) }
        }
    }
}
