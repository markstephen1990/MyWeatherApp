package com.ferngames.myweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastAdapter(private val forecastList: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvForecastDay)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivForecastIcon)
        val tvTemp: TextView = itemView.findViewById(R.id.tvForecastTemp)
        val tvDescription: TextView = itemView.findViewById(R.id.tvForecastDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = forecastList[position]

        // Format date to show day name (e.g. "Mon", "Tue")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE\nMMM d", Locale.getDefault())
        val date = inputFormat.parse(item.dt_txt)
        holder.tvDay.text = date?.let { outputFormat.format(it) } ?: "N/A"

        // Temperature
        holder.tvTemp.text = "${item.main.temp.toInt()}°C"

        // Description
        holder.tvDescription.text = item.weather[0].description
            .replaceFirstChar { it.uppercase() }

        // Weather icon
        val iconCode = item.weather[0].icon
        val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
        Glide.with(holder.itemView.context).load(iconUrl).into(holder.ivIcon)
    }

    override fun getItemCount() = forecastList.size
}