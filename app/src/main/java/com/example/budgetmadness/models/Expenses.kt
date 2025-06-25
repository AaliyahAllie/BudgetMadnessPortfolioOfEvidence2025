package com.example.budgetmadness.models

data class Expense(
    val name: String = "",
    val amount: Double = 0.0,
    val method: String = "",
    val category: String = "",
    val date: String = "",       // Format: "yyyy-MM-dd"
    val month: String = "",      // For monthly grouping like "July"
    val receiptUri: String? = null
)