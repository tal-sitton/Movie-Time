package com.example.movietime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.net.UnknownHostException
import java.util.concurrent.Executors

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
            onBackPressedCallback.handleOnBackPressed()
        }
        mainButton.text = "אישור"
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
                screening.location,
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
        val location: Location
    ) {
        var distance: Double = 0.0

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

    private val mHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            createCinemasButtons(findViewById(R.id.ll), false)
        }
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
                    Executors.newSingleThreadExecutor().execute {
                        sortByLocation(location, mHandler)
                    }
                }
            }
    }

    private fun sortByLocation(myLocation: Location, mHandler: Handler) {
        Looper.prepare()
        if (cinemas[0].distance == 0.0) {
            try {
                Utils.calcDistance(myLocation, cinemas)
            } catch (ex: UnknownHostException) {
                cinemas.forEach { cinema ->
                    cinema.distance = myLocation.distanceTo(cinema.location).toDouble()
                }
            }
        }
        cinemas.sortBy { cinema -> cinema.distance }
        mHandler.sendEmptyMessage(0)
        Utils.showToast(this, "מויין לפי מרחק")
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted)
                getLastKnownLocation()
            else {
                val box: CheckBox = findViewById(R.id.check)
                box.isChecked = false
                Utils.showToast(this, "לא ניתנה גישה למיקום")
            }
        }
}
