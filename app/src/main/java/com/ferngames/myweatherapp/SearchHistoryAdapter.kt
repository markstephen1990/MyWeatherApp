package com.ferngames.myweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchHistoryAdapter(
    private var historyList: MutableList<String>,
    private val onCityClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCity: TextView = itemView.findViewById(R.id.tvCityHistory)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDeleteHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val city = historyList[position]

        holder.tvCity.text = city

        // Tap city to load weather
        holder.itemView.setOnClickListener {
            onCityClick(city)
        }

        // Tap X to delete from history
        holder.tvDelete.setOnClickListener {
            onDeleteClick(city)
        }
    }

    override fun getItemCount() = historyList.size

    fun updateList(newList: MutableList<String>) {
        historyList = newList
        notifyDataSetChanged()
    }
}