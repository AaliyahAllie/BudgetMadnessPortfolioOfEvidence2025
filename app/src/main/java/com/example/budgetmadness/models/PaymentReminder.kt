package com.example.budgetmadness.models

data class PaymentReminder(
    val title: String = "",
    val amount: Double = 0.0,
    val dueDate: String = "",   // Format: yyyy-MM-dd
    val dueTime: String = ""    // Format: HH:mm
)