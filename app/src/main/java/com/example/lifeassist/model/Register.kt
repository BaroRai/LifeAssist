package com.example.lifeassist.model

data class Register(
    val userId: String,
    val email: String,
    val password: String? = null
)
