package com.example.lifeassist.api.interfaces

import com.example.lifeassist.api.data.GoalRequest
import com.example.lifeassist.api.data.LoginData
import com.example.lifeassist.api.data.RegisterData
import com.example.lifeassist.api.data.UserDataResponse
import com.example.lifeassist.api.data.UserDataRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/auth/user")
    suspend fun getUserData(@Query("id") id: String): Response<UserDataResponse>

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterData.Request): Response<RegisterData.Response>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginData.Request): Response<LoginData.Response>

    @POST("api/user/{userId}/goals")
    suspend fun submitGoal(@Path("userId") userId: String, @Body goalRequest: GoalRequest): Response<Unit>
}

