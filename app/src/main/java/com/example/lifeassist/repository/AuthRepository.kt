package com.example.lifeassist.repository

import com.example.lifeassist.api.RetrofitClient
import com.example.lifeassist.api.data.LoginData
import com.example.lifeassist.api.data.RegisterData
import com.example.lifeassist.model.Login
import com.example.lifeassist.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun login(email: String, password: String): Result<Login> {
        return withContext(Dispatchers.IO) {
            try {
                val loginRequest = LoginData.Request(email = email, password = password)
                val response = apiService.login(loginRequest)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Log the raw response
                        println("API Response: $responseBody")
                        Result.Success(responseBody.toLogin())
                    } else {
                        Result.Error("Empty response from server")
                    }
                } else {
                    Result.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Network error: ${e.message}")
            }
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val registerRequest = RegisterData.Request(email = email, password = password)
                val response = apiService.register(registerRequest)
                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Result.Error("Network error: ${e.message}")
            }
        }
    }

    private fun LoginData.Response.toLogin(): Login {
        requireNotNull(userId) { "userId is missing in the response" } // Validate userId
        return Login(
            userId = this.userId,
            email = this.email,
            username = this.username ?: "Anonymous",
            description = this.description ?: ""
        )
    }
}
