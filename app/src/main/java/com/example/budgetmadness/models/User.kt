package com.example.budgetmadness.models

data class User(
    val username: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val imageUri: String = "" // optional URI to profile image
)
