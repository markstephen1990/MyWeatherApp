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

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getWeather(city: String, apiKey: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getWeather(city, apiKey)
                _weatherData.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "City not found. Please try again."
                _isLoading.value = false
            }
        }
    }
}