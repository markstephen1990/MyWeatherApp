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
                // Fetch both current weather and forecast at the same time
                val weatherResult = repository.getWeather(city, apiKey)
                val forecastResult = repository.getForecast(city, apiKey)

                _weatherData.value = weatherResult

                // Filter to get one forecast per day (at 12:00:00)
                val dailyForecasts = forecastResult.list.filter { item ->
                    item.dt_txt.contains("12:00:00")
                }
                _forecastData.value = dailyForecasts

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "City not found. Please try again."
                _isLoading.value = false
            }
        }
    }
}