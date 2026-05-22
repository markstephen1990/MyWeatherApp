package com.ferngames.myweatherapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    // Replace with your actual OpenWeatherMap API key
    private val API_KEY = "c3054f23d5bba56f9c0f5e15a51abd05"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Views
        val etCityName = findViewById<EditText>(R.id.etCityName)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
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
                tvError.text = "Please enter a city name."
                tvError.visibility = View.VISIBLE
                weatherCard.visibility = View.GONE
                rvForecast.visibility = View.GONE
                tvForecastTitle.visibility = View.GONE
            } else {
                tvError.visibility = View.GONE
                viewModel.getWeather(city, API_KEY)
            }
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

            // Load weather icon
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
            tvError.text = error
            tvError.visibility = View.VISIBLE
            weatherCard.visibility = View.GONE
            rvForecast.visibility = View.GONE
            tvForecastTitle.visibility = View.GONE
        }
    }
}