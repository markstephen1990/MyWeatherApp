package com.ferngames.myweatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var historyManager: SearchHistoryManager
    private lateinit var historyAdapter: SearchHistoryAdapter

    private val API_KEY = "c3054f23d5bba56f9c0f5e15a51abd05"

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocation()
        } else {
            val permanentlyDenied = !shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permanentlyDenied) {
                showSettingsDialog()
            } else {
                showError("Location permission denied. Please allow it to use this feature.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before UI loads
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        var isDarkMode = prefs.getBoolean("dark_mode", true)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        historyManager = SearchHistoryManager(this)

        // Theme toggle setup
        val btnToggleTheme = findViewById<Button>(R.id.btnToggleTheme)
        btnToggleTheme.text = if (isDarkMode) getString(R.string.light_mode)
        else getString(R.string.dark_mode)

        // Toggle button click
        btnToggleTheme.setOnClickListener {
            isDarkMode = !isDarkMode
            prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            btnToggleTheme.text = if (isDarkMode) getString(R.string.light_mode)
            else getString(R.string.dark_mode)
        }

        // Views
        val etCityName = findViewById<EditText>(R.id.etCityName)
        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
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
        val rvSearchHistory = findViewById<RecyclerView>(R.id.rvSearchHistory)
        val historyContainer = findViewById<LinearLayout>(R.id.historyContainer)
        val tvClearHistory = findViewById<TextView>(R.id.tvClearHistory)
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        // Set refresh color to match app theme
        swipeRefreshLayout.setColorSchemeColors(
            android.graphics.Color.parseColor("#6C63FF")
        )

        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener {
            val city = etCityName.text.toString().trim()
            if (city.isNotEmpty()) {
                viewModel.getWeather(city, API_KEY)
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // Setup forecast RecyclerView
        rvForecast.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )

        // Setup history RecyclerView
        historyAdapter = SearchHistoryAdapter(
            historyManager.getHistory().toMutableList(),
            onCityClick = { city ->
                etCityName.setText(city)
                tvError.visibility = View.GONE
                viewModel.getWeather(city, API_KEY)
            },
            onDeleteClick = { city ->
                val newHistory = historyManager.getHistory().toMutableList()
                newHistory.remove(city)
                historyManager.clearHistory()
                newHistory.forEach { historyManager.saveCity(it) }
                historyAdapter.updateList(newHistory)
                if (newHistory.isEmpty()) historyContainer.visibility = View.GONE
            }
        )
        rvSearchHistory.layoutManager = LinearLayoutManager(this)
        rvSearchHistory.adapter = historyAdapter

        // Show history if exists
        if (historyManager.getHistory().isNotEmpty()) {
            historyContainer.visibility = View.VISIBLE
        }

        // Clear all history
        tvClearHistory.setOnClickListener {
            historyManager.clearHistory()
            historyAdapter.updateList(mutableListOf())
            historyContainer.visibility = View.GONE
        }

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

        // Auto fetch location on app open
        checkLocationPermission()

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) swipeRefreshLayout.isRefreshing = false
        }

        // New detail views
        val tvFeelsLike = findViewById<TextView>(R.id.tvFeelsLike)
        val tvPressure = findViewById<TextView>(R.id.tvPressure)
        val tvVisibility = findViewById<TextView>(R.id.tvVisibility)
        val tvSunrise = findViewById<TextView>(R.id.tvSunrise)
        val tvSunset = findViewById<TextView>(R.id.tvSunset)

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

            // New details
            tvFeelsLike.text = "${weather.main.feels_like.toInt()}°C"
            tvPressure.text = "${weather.main.pressure} hPa"
            tvVisibility.text = "${weather.visibility / 1000} km"

            // Format sunrise and sunset times
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            tvSunrise.text = sdf.format(java.util.Date(weather.sys.sunrise * 1000))
            tvSunset.text = sdf.format(java.util.Date(weather.sys.sunset * 1000))

            val iconCode = weather.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
            Glide.with(this).load(iconUrl).into(ivWeatherIcon)

            // Save to history
            val city = etCityName.text.toString().trim()
            if (city.isNotEmpty()) {
                historyManager.saveCity(city)
                val updatedHistory = historyManager.getHistory().toMutableList()
                historyAdapter.updateList(updatedHistory)
                historyContainer.visibility = View.VISIBLE
            }

            // Update widget
            WeatherWidget.saveWeatherData(
                this,
                weather.name,
                "${weather.main.temp.toInt()}°C",
                weather.weather[0].description.replaceFirstChar { it.uppercase() }
            )
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
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs location access to show your local weather.")
                    .setPositiveButton("Allow") { _, _ ->
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    @Suppress("MissingPermission")
    private fun fetchLocation() {
        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                viewModel.getWeatherByLocation(
                    location.latitude,
                    location.longitude,
                    API_KEY
                )
            } else {
                showError("Unable to get location. Please enable GPS and try again.")
            }
        }.addOnFailureListener {
            showError("Location error: ${it.message}")
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("Location permission was permanently denied. Please enable it in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}