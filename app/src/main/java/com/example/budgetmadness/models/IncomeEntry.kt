package com.example.budgetmadness.models

data class IncomeEntry(
    val cash: Double = 0.0,
    val card: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)