package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class RegisterData(
    val request: Request,
    val response: Response? = null
) {
    // Request: Data sent during registration
    data class Request(
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String
    )

    // Response: Data received after successful registration
    data class Response(
        @SerializedName("message") val message: String,
        @SerializedName("userId") val userId: String?
    )
}
