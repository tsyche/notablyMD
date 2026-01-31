package com.tsyche.notablymd.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Adapter for displaying performance statistics */
class PerformanceStatsAdapter : RecyclerView.Adapter<PerformanceStatsAdapter.ViewHolder>() {

    private var stats: List<String> = emptyList()

    fun updateStats(newStats: List<String>) {
        stats = newStats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount(): Int = stats.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(stat: String) {
            textView.text = stat

            // Style different types of stats
            when {
                stat.isEmpty() -> {
                    textView.text = ""
                    textView.setBackgroundColor(0x00000000) // Transparent
                }
                stat.endsWith("Statistics") -> {
                    textView.text = stat
                    textView.setBackgroundColor(0x1A000000) // Dark background
                    textView.setTextColor(0xFFFFFFFF.toInt()) // White text
                    textView.setTypeface(null, android.graphics.Typeface.BOLD)
                }
                stat.startsWith("  ") -> {
                    textView.text = stat
                    textView.setBackgroundColor(0x00000000) // Transparent
                    textView.setTextColor(0xFF666666.toInt()) // Gray text
                    textView.setPadding(32, 8, 8, 8) // Indent
                }
                else -> {
                    textView.text = stat
                    textView.setBackgroundColor(0x00000000) // Transparent
                    textView.setTextColor(0xFF000000.toInt()) // Black text
                }
            }
        }
    }
}
