package com.example.budgetmadness

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetmadness.models.IncomeEntry
import java.text.SimpleDateFormat
import java.util.*
/**
 * Adapter class used to display a list of income entries in a RecyclerView.
 * Each item shows the amount of cash and card income with a timestamp.
 */

class IncomeHistoryAdapter(private val data: List<IncomeEntry>) :
    RecyclerView.Adapter<IncomeHistoryAdapter.IncomeViewHolder>() {
    // ViewHolder holds a simple TextView for each income
    class IncomeViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)
    // Inflate the built-in simple_list_item_1 layout for each row

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return IncomeViewHolder(textView)
    }
    // Bind income data (cash, card, and date) to the TextView
    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val entry = data[position]
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
        holder.view.text = "Cash: R%.2f | Card: R%.2f | $date".format(entry.cash, entry.card)
    }
    // Return the total number of items
    override fun getItemCount(): Int = data.size
}