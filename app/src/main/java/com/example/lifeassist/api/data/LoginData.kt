package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class LoginData(
    val request: Request,
    val response: Response? = null // Nullable for cases where the response isn't immediately available
) {
    // Request: Data sent during login
    data class Request(
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String
    )

    // Response: Data received from the server after login
    data class Response(
        @SerializedName("userId") val userId: String,
        @SerializedName("email") val email: String,
        @SerializedName("username") val username: String?,
        @SerializedName("description") val description: String?
    )
}
