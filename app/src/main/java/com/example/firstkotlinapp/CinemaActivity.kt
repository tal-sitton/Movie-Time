package com.example.firstkotlinapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.pow
import kotlin.math.sqrt

class CinemaActivity : MyTemplateActivity() {

    companion object {
        val selectedCinemas: MutableList<String> = mutableListOf("")
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var cinemas: MutableList<Cinema> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter_cinema)
        setupTopButtons()
        getCinemas()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sortBy(findViewById(R.id.check))
    }

    private fun createCinemasButtons(ll: LinearLayout, sortDistricts: Boolean) {
        ll.removeAllViewsInLayout()
        if (sortDistricts)
            getCinemas()

        var district = cinemas[0].district

        if (sortDistricts)
            createDisTextView(district, ll)

        for (cinema in cinemas) {
            if (sortDistricts) {
                if (cinema.district != district) {
                    district = cinema.district
                    createDisTextView(district, ll)
                }
            }
            val button = Button(this)
            button.text = cinema.name
            if (selectedCinemas.contains(cinema.name)) {
                button.isSelected = true
                button.setBackgroundResource(R.drawable.clicked_button)
            } else
                button.setBackgroundResource(R.drawable.unclicked_button)
            button.setOnClickListener {
                if (button.isSelected) {
                    button.setBackgroundResource(R.drawable.unclicked_button)
                    button.isSelected = false
                    selectedCinemas.remove(cinema.name)
                } else {
                    button.setBackgroundResource(R.drawable.clicked_button)
                    button.isSelected = true
                    selectedCinemas.add(cinema.name)
                }
            }
            ll.addView(button)
            val spacer = Space(this)
            spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
            ll.addView(spacer)
        }
    }

    private fun createDisTextView(district: String, ll: LinearLayout) {
        val districtText = TextView(this)
        districtText.text = district
        districtText.textSize = 20.0f
        districtText.setTypeface(null, Typeface.BOLD)
        districtText.gravity = android.view.Gravity.CENTER
        ll.addView(districtText)
        val spacer = Space(this)
        spacer.layoutParams = ViewGroup.LayoutParams(10, 35)
        ll.addView(spacer)
    }

    private fun setupTopButtons() {
        val mainButton: TextView = findViewById(R.id.cinemaButton)
        mainButton.setBackgroundResource(R.drawable.top_buttons_clicked)
        mainButton.setOnClickListener {
            onBackPressed()
        }
        val dateButton: TextView = findViewById(R.id.dateButton)
        val movieButton: TextView = findViewById(R.id.movieButton)

        dateButton.text = DateActivity.selectedDatStr

        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        dateButton.setOnClickListener {
            intent.setClass(this, DateActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
        movieButton.setOnClickListener {
            intent.setClass(this, MovieActivity::class.java)
            MainActivity.filter()
            startActivity(intent)
        }
    }

    private fun getCinemas(): List<Cinema> {
        cinemas = mutableListOf()
        for (screening in MainActivity.filteredMoviesScreenings.intersect(MainActivity.filteredDateScreenings)) {
            val tmpCinema = Cinema(
                screening.cinema,
                screening.district,
                screening.latitude,
                screening.longitude
            )
            if (!cinemas.any { cinema -> cinema == tmpCinema }) {
                cinemas.add(tmpCinema)
            }
        }
        return cinemas
    }

    class Cinema(
        val name: String,
        val district: String,
        val latitude: Double,
        val longitude: Double
    ) {
        override fun equals(other: Any?): Boolean {
            return other is Cinema && other.name == name && other.district == district
        }
    }


    fun sortBy(view: View) {
        view as CheckBox
        if (view.isChecked)
            getLastKnownLocation()
        else
            createCinemasButtons(findViewById(R.id.ll), true)


    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    sortByLocation(location.latitude, location.longitude)
                    createCinemasButtons(findViewById(R.id.ll), false)
                }
            }
    }

    private fun sortByLocation(latitude: Double, longitude: Double) {
        cinemas.sortBy { cinema ->
            sqrt(
                (cinema.latitude - latitude).pow(2.0) + (cinema.longitude - longitude).pow(2.0)
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted)
                getLastKnownLocation()
            else {
                val box: CheckBox = findViewById(R.id.check)
                box.isSelected = false
                Utils.showToast(this, "Permission denied")
            }
        }
}
