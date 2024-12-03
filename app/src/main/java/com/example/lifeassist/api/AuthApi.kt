package com.example.lifeassist.api

import com.google.gson.annotations.SerializedName

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data classes used
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class UserDataResponse(
    @SerializedName("Id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("description") val description: String
)

// Api functions to call ... use dataclasses -,-
interface ApiService {
    @GET("api/auth/user")
    suspend fun getUserData(@Query("email") email: String): Response<UserDataResponse>

    @POST("api/auth/register")
        suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse> // Use AuthResponse


    @POST("api/auth/login") // This would be the API endpoint for login
        suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>  // Use AuthResponse
    }
