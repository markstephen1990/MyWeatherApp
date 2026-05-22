package com.ferngames.myweatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Replace with your actual OpenWeatherMap API key
    private val API_KEY = "c3054f23d5bba56f9c0f5e15a51abd05"

    // Permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocation()
        } else {
            showError("Location permission denied. Please enable it in settings.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Fix edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Views
        val etCityName = findViewById<EditText>(R.id.etCityName)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val btnLocation = findViewById<Button>(R.id.btnLocation)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val weatherCard = findViewById<LinearLayout>(R.id.weatherCard)
        val tvCityName = findViewById<TextView>(R.id.tvCityName)
        val tvTemperature = findViewById<TextView>(R.id.tvTemperature)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvHumidity = findViewById<TextView>(R.id.tvHumidity)
        val tvWindSpeed = findViewById<TextView>(R.id.tvWindSpeed)
        val tvError = findViewById<TextView>(R.id.tvError)
        val ivWeatherIcon = findViewById<ImageView>(R.id.ivWeatherIcon)
        val rvForecast = findViewById<RecyclerView>(R.id.rvForecast)
        val tvForecastTitle = findViewById<TextView>(R.id.tvForecastTitle)

        // Setup RecyclerView
        rvForecast.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )

        // Search button click
        btnSearch.setOnClickListener {
            val city = etCityName.text.toString().trim()
            if (city.isEmpty()) {
                showError("Please enter a city name.")
                weatherCard.visibility = View.GONE
                rvForecast.visibility = View.GONE
                tvForecastTitle.visibility = View.GONE
            } else {
                tvError.visibility = View.GONE
                viewModel.getWeather(city, API_KEY)
            }
        }

        // Location button click
        btnLocation.setOnClickListener {
            checkLocationPermission()
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe weather data
        viewModel.weatherData.observe(this) { weather ->
            weatherCard.visibility = View.VISIBLE
            tvError.visibility = View.GONE

            tvCityName.text = weather.name
            tvTemperature.text = "${weather.main.temp.toInt()}°C"
            tvDescription.text = weather.weather[0].description
                .replaceFirstChar { it.uppercase() }
            tvHumidity.text = "${weather.main.humidity}%"
            tvWindSpeed.text = "${weather.wind.speed} m/s"

            val iconCode = weather.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
            Glide.with(this).load(iconUrl).into(ivWeatherIcon)
        }

        // Observe forecast data
        viewModel.forecastData.observe(this) { forecastList ->
            tvForecastTitle.visibility = View.VISIBLE
            rvForecast.visibility = View.VISIBLE
            rvForecast.adapter = ForecastAdapter(forecastList)
        }

        // Observe error
        viewModel.errorMessage.observe(this) { error ->
            showError(error)
            weatherCard.visibility = View.GONE
            rvForecast.visibility = View.GONE
            tvForecastTitle.visibility = View.GONE
        }
    }

    private fun checkLocationPermission() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (fineLocation == PackageManager.PERMISSION_GRANTED ||
            coarseLocation == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @Suppress("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.getWeatherByLocation(
                    location.latitude,
                    location.longitude,
                    API_KEY
                )
            } else {
                showError("Unable to get location. Please try again.")
            }
        }.addOnFailureListener {
            showError("Location error: ${it.message}")
        }
    }

    private fun showError(message: String) {
        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}