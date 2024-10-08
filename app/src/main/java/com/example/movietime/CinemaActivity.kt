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
import android.text.Editable
import android.text.TextWatcher
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
        var sortByDistance: Boolean = false
        val cinemas: MutableList<Cinema> = mutableListOf()
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_filter_cinema)
        setupTopButtons()
        getCinemas()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val sortByDistanceBox = findViewById<CheckBox>(R.id.sort)

        sortByDistanceBox.isChecked = sortByDistance

        sortByDistanceBox.setOnCheckedChangeListener { _, isChecked ->
            sortByDistance = isChecked
            sort(true)
        }

        sort(true)

        val search = findViewById<EditText>(R.id.search)

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                sort()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterCinemas(): MutableList<Cinema> {
        val search = findViewById<EditText>(R.id.search)
        val text = search.text.toString().trim()
        val availableCinemas =
            cinemas.filter { cinema -> cinema.name.contains(text) }.toMutableList()
        availableCinemas.sortWith(
            compareBy(
                { !selectedCinemas.contains(it.name) },
                { availableCinemas.indexOf(it) })
        )
        return availableCinemas
    }

    private fun createCinemasButtons(
        ll: LinearLayout,
        sortDistricts: Boolean,
        availableCinemas: MutableList<Cinema>
    ) {
        hideLoading()
        ll.removeAllViewsInLayout()

        if (availableCinemas.isEmpty())
            return

        var district = ""

        var createChoicesTitle = false
        var createDistanceTitle = false

        for (cinema in availableCinemas) {
            if (selectedCinemas.contains(cinema.name)) {
                if (!createChoicesTitle) {
                    createDisTextView("הבחירות שלך", ll)
                    createChoicesTitle = true
                }
            } else {
                if (sortDistricts) {
                    if (cinema.district != district) {
                        district = cinema.district
                        createDisTextView(district, ll)
                    }
                } else {
                    if (!createDistanceTitle) {
                        createDisTextView("בתי קולנוע זמינים בקרבך", ll)
                        createDistanceTitle = true
                    }
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
        val intersectedScreenings = MainActivity.filteredMoviesScreenings.intersect(
            MainActivity.filteredDateScreenings
        ).intersect(MainActivity.filteredDubScreenings)

        val intersectedCinemasNames = intersectedScreenings.map { it.cinema }
        if (intersectedCinemasNames.toSet() == cinemas.map { it.name }.toSet())
            return cinemas

        cinemas.clear()
        for (screening in intersectedScreenings) {
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

    private fun isDistanceCalculated(): Boolean {
        return cinemas.isNotEmpty() && cinemas[0].distance != 0.0
    }

    private fun showLoading() {
        val loadingIndicator = findViewById<ImageView>(R.id.calculating)
        loadingIndicator.visibility = ImageView.VISIBLE
    }

    private fun hideLoading() {
        val loadingIndicator = findViewById<ImageView>(R.id.calculating)
        loadingIndicator.visibility = ImageView.INVISIBLE
    }


    private fun sort(loading: Boolean = false) {
        if (sortByDistance) {
            if (isDistanceCalculated()) {
                cinemas.sortBy { cinema -> cinema.distance }
                mHandler.sendEmptyMessage(0)
            } else {
                if (loading) {
                    findViewById<LinearLayout>(R.id.ll).removeAllViewsInLayout()
                    showLoading()
                }
                getLastKnownLocation(::sortByDistance)
            }
        } else {
            cinemas.sortWith(compareBy({ it.district }, { it.name }))
            mHandler.sendEmptyMessage(0)
        }
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            createCinemasButtons(
                findViewById(R.id.ll),
                !findViewById<CheckBox>(R.id.sort).isChecked,
                filterCinemas()
            )
        }
    }

    private fun getLastKnownLocation(callback: (Location, Handler) -> Unit) {
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
                        callback(location, mHandler)
                    }
                } else {
                    Utils.enableLocation(this) { afterLocationRequest(it) }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Utils.LOCATION_REQUEST_CODE) {
            afterLocationRequest(resultCode == RESULT_OK)
        }
    }

    private fun afterLocationRequest(success: Boolean) {
        if (success)
            getLastKnownLocation(::sortByDistance)
        else {
            Utils.showToast(this, "לא ניתנה גישה למיקום")
            val sortByDistanceBox: CheckBox = findViewById(R.id.sort)
            sortByDistanceBox.isChecked = false
            sortByDistance = false
        }
    }

    private fun sortByDistance(myLocation: Location, mHandler: Handler) {
        Looper.prepare()
        if (cinemas.isEmpty())
            return
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
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted)
                getLastKnownLocation(::sortByDistance)
            else {
                val box: CheckBox = findViewById(R.id.sort)
                box.isChecked = false
                Utils.showToast(this, "לא ניתנה גישה למיקום")
            }
        }
}
