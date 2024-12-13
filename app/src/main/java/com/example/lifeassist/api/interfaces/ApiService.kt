package com.example.lifeassist.api.interfaces

import com.example.lifeassist.api.data.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // Fetch user data by userId: GET /api/users/{userId}
    @GET("api/users/{userId}")
    suspend fun getUserData(@Path("userId") userId: String): Response<UserDataResponse>

    // Register a new user: POST /api/register
    @POST("api/register")
    suspend fun register(@Body registerRequest: RegisterData.Request): Response<RegisterData.Response>

    // Login: POST /api/login
    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginData.Request): Response<LoginData.Response>

    // Submit a new goal for a user: POST /api/users/{userId}/goals
    @POST("api/users/{userId}/goals")
    suspend fun submitGoal(@Path("userId") userId: String, @Body goalRequest: GoalRequest): Response<GoalResponse>
}
