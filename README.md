# 🌤️ Fern Weather App

An Android weather app built with Kotlin that shows real-time weather data, detailed conditions, a 5-day forecast, and saves your recent searches.

## Features
- 🔍 Search weather by city name with icon button
- 📍 Get weather based on current GPS location
- 🏠 Auto-loads local weather on app launch
- 🌡️ Displays temperature, humidity, and wind speed
- 🌡️ Feels like temperature
- 💧 Atmospheric pressure
- 👁️ Visibility in km
- 🌅 Sunrise and sunset times
- 🌤️ Weather condition icons
- 📅 5-day weather forecast
- 🕐 Search history with delete and clear all
- 🔄 Pull to refresh
- ❌ Error handling for invalid cities
- ⏳ Loading spinner while fetching data
- 🌙 Dark/light mode toggle
- 💫 Splash screen on launch
- 📱 Home screen widget
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
- SwipeRefreshLayout
- AppWidget (home screen widget)

## Screenshots
<img width="298" height="656" alt="image" src="https://github.com/user-attachments/assets/7b9ae168-3fa5-4fcf-849b-1a75cf05fd5b" />
<img width="298" height="656" alt="image" src="https://github.com/user-attachments/assets/c5ade096-d5ad-4fb7-b0bc-3b7abc21a781" />



## Setup
1. Clone the repo
2. Get a free API key from openweathermap.org
3. Add your API key in MainActivity.kt
4. Run the app in Android Studio

## Architecture
