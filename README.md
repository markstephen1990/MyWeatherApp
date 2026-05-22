# 🌤️ Fern Weather App

An Android weather app built with Kotlin that shows real-time weather data and a 5-day forecast.

## Features
- 🔍 Search weather by city name
- 📍 Get weather based on current GPS location
- 🌡️ Displays temperature, humidity, and wind speed
- 🌡️ Feels like temperature
- 💧 Atmospheric pressure
- 👁️ Visibility in km
- 🌅 Sunrise and sunset times
- 🌤️ Weather condition icons
- 📅 5-day weather forecast
- 🕐 Search history with delete and clear all
- ❌ Error handling for invalid cities
- ⏳ Loading spinner while fetching data
- 🌙 Dark/light mode toggle
- 💫 Splash screen on launch
- 📱 Scrollable layout that fits all screen sizes

## Tech Stack
- Kotlin
- Retrofit (API calls)
- OpenWeatherMap API
- MVVM Architecture
- Glide (image loading)
- Coroutines
- LiveData & ViewModel
- FusedLocationProviderClient (GPS)
- SharedPreferences (search history & settings)
- RecyclerView
- SplashScreen API

## Screenshots
<img width="592" height="1308" alt="image" src="https://github.com/user-attachments/assets/c88efd6c-759e-43c4-bac6-db9a9496be00" />

## Setup
1. Clone the repo
2. Get a free API key from openweathermap.org
3. Add your API key in MainActivity.kt
4. Run the app in Android Studio

## Architecture
