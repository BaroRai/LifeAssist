package com.example.lifeassist.api

data class AuthResponse(
    val message: String?, // Message from the API
    val error: String?,   // Error details if any
    val userId: String?
)
