package com.example.firstkotlinapp

import android.content.Context
import android.widget.Toast
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

    }
}
