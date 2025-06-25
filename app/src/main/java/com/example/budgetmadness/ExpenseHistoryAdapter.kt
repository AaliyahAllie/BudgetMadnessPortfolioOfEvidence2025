package com.example.budgetmadness

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetmadness.models.Expense
/**
 * Adapter class used to populate a RecyclerView with a list of Expense objects.
 * Displays expense name, amount, and date in a simple list format.
 */

class ExpenseHistoryAdapter(private val data: List<Expense>) :
    RecyclerView.Adapter<ExpenseHistoryAdapter.ExpenseViewHolder>() {
    //ViewHolder class that holds a single TextView for each expense entry.
    class ExpenseViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)
    /**
     * Binds an Expense object to the TextView in the ViewHolder.
     * Displays name, amount, and date of the expense.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return ExpenseViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = data[position]
        holder.view.text = "Name: ${expense.name}, Amount: R${expense.amount}, Date: ${expense.date}"
    }
    //Returns the total number of expense items to display.
    override fun getItemCount(): Int = data.size
}