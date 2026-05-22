package com.ferngames.myweatherapp

import android.content.Context

class SearchHistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val KEY = "cities"
    private val MAX_HISTORY = 5

    fun saveCity(city: String) {
        val history = getHistory().toMutableList()
        history.remove(city)
        history.add(0, city)
        if (history.size > MAX_HISTORY) history.removeLast()
        prefs.edit().putString(KEY, history.joinToString(",")).apply()
    }

    fun getHistory(): List<String> {
        val raw = prefs.getString(KEY, "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split(",")
    }

    fun clearHistory() {
        prefs.edit().remove(KEY).apply()
    }
}