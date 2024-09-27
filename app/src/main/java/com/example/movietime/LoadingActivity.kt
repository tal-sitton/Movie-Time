package com.example.movietime

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.time.LocalDateTime

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val acceptedTerms =
            getSharedPreferences("PREFERENCES", MODE_PRIVATE).getBoolean("termsAccepted", false)
        if (!acceptedTerms) {
            showTermsDialog()
        } else {
            loadData()
        }
    }

    private fun showTermsDialog() {
        AlertDialog.Builder(this)
            .setTitle("תנאי שימוש")
            .setMessage("השימוש ביישומון הוא באחריות המשתמש בלבד. היישומון מספק מידע על סרטים והקרנות כפי שהוא נלקח מרשתות בתי הקולנוע, אך ייתכנו שגיאות או טעויות עיבוד במידע המוצג (כגון שם הסרט, פרטי ההקרנה, מיקום, ועוד).\n" +
                    "היישומון אינו אחראי על נכונות המידע, והמשתמש נדרש לוודא את כל הפרטים באתר הרשמי של רשת בתי הקולנוע לפני ביצוע הזמנה או כל פעולה אחרת המבוססת על מידע זה.\n" +
                    "בכל מקרה של חוסר דיוק במידע או טעויות, היישומון לא יישא באחריות לכל נזק שייגרם כתוצאה מהסתמכות על המידע המוצג בו.")
            .setPositiveButton("אשר") { dialog, _ ->
                acceptTerms()
                dialog.dismiss()
                loadData()
            }
            .setCancelable(false)
            .show()
    }

    private fun acceptTerms() {
        val sharedPreferences = this.getSharedPreferences("PREFERENCES", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("termsAccepted", true)
        editor.apply()
    }

    private fun loadData() {
        val now: LocalDateTime = LocalDateTime.now()
        val json: JSONObject? = JSONUtils.loadJSONFromFile(this, "Movies.json")
        if (json != null) {
            val lastUpdate = json.getString("time")
            if (lastUpdate.split("-")[0].toInt() != now.dayOfMonth) {
                findViewById<ProgressBar>(R.id.progressBar).visibility = ProgressBar.VISIBLE
                JSONUtils.updateJSON(this)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    this.startActivity(Intent(this, MainActivity::class.java))
                }, 15)
            }
        } else {
            findViewById<ProgressBar>(R.id.progressBar).visibility = ProgressBar.VISIBLE
            JSONUtils.updateJSON(this)
        }
    }
}
