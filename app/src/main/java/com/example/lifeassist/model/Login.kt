package com.example.lifeassist.model

data class Login(
    val userId: String,
    val email: String,
    val password: String? = null,
    val username: String?,
    val description: String?
)
