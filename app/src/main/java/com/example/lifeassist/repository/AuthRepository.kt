package com.example.lifeassist.repository

import com.example.lifeassist.api.AuthResponse
import com.example.lifeassist.api.LoginRequest
import com.example.lifeassist.api.RegisterRequest
import com.example.lifeassist.api.RetrofitClient
import retrofit2.Response

class AuthRepository {

    private val api = RetrofitClient.authApi

    // Function to handle login and return AuthResponse
    suspend fun login(email: String, password: String): AuthResponse? {
        val loginRequest = LoginRequest(email, password)

        // Make the network request using Retrofit's suspend function
        val response: Response<AuthResponse> = api.login(loginRequest)

        // Return the response body if successful
        return if (response.isSuccessful) {
            response.body() // Return AuthResponse if successful
        } else {
            null // Return null if unsuccessful
        }
    }

    // Function to handle registration (similar to login)
    suspend fun register(email: String, password: String): AuthResponse? {
        val registerRequest = RegisterRequest(email, password)

        // Make the network request using Retrofit's suspend function
        val response: Response<AuthResponse> = api.register(registerRequest)  // Assuming register endpoint is same
        return if (response.isSuccessful) {
            response.body() // Return AuthResponse if successful
        } else {
            null // Return null if unsuccessful
        }
    }
}
