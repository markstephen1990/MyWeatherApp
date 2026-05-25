package com.ferngames.myweatherapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            // Load last saved weather data
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            val city = prefs.getString("city", "Fern Weather") ?: "Fern Weather"
            val temp = prefs.getString("temp", "--°C") ?: "--°C"
            val description = prefs.getString("description", "--") ?: "--"

            // Set widget text
            views.setTextViewText(R.id.widget_city, city)
            views.setTextViewText(R.id.widget_temp, temp)
            views.setTextViewText(R.id.widget_description, description)

            // Tap widget to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_city, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_temp, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_description, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun saveWeatherData(context: Context, city: String, temp: String, description: String) {
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("city", city)
                .putString("temp", temp)
                .putString("description", description)
                .apply()

            // Update all active widgets
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, WeatherWidget::class.java)
            )
            for (id in widgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }
}