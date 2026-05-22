package com.ferngames.myweatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    private val _forecastData = MutableLiveData<List<ForecastItem>>()
    val forecastData: LiveData<List<ForecastItem>> = _forecastData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getWeather(city: String, apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val weatherResult = repository.getWeather(city, apiKey)
                val forecastResult = repository.getForecast(city, apiKey)
                _weatherData.value = weatherResult

                val dailyForecasts = forecastResult.list
                    .groupBy { it.dt_txt.substring(0, 10) }
                    .values
                    .map { it.first() }
                    .take(5)
                _forecastData.value = dailyForecasts
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "City not found. Please try again."
                _isLoading.value = false
            }
        }
    }

    fun getWeatherByLocation(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val weatherResult = repository.getWeatherByLocation(
                    latitude, longitude, apiKey
                )
                val forecastResult = repository.getForecastByLocation(
                    latitude, longitude, apiKey
                )

                _weatherData.value = weatherResult

                val dailyForecasts = forecastResult.list
                    .groupBy { it.dt_txt.substring(0, 10) }
                    .values
                    .map { it.first() }
                    .take(5)

                dailyForecasts.forEach { item ->
                    android.util.Log.d("FORECAST", "Date: ${item.dt_txt}, Temp: ${item.main.temp}")
                }
                _forecastData.value = dailyForecasts

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Unable to get location weather. Please try again."
                _isLoading.value = false
            }
        }
    }
}