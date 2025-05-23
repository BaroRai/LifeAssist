package com.example.lifeassist.api.interfaces

import com.example.lifeassist.api.data.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @PUT("api/users/{userId}/goals/{goalId}/status")
    suspend fun updateGoalStatus(@Path("userId") userId: String, @Path("goalId") goalId: String,
                                 @Body request: GoalStatusUpdateRequest): Response<Unit>

    // PATCH: Update user profile (username and description)
    @PATCH("api/users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<UserDataResponse>

    // PATCH: Update user description
    @PATCH("api/users/{userId}/description")
    suspend fun updateUserDescription(
        @Path("userId") userId: String,
        @Body updateDescriptionRequest: UpdateDescriptionRequest
    ): Response<UserDataResponse>

}
