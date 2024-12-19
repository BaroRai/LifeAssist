package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class UserDataResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("description") val description: String,
    @SerializedName("goals") val goals: List<GoalResponse> // Include goals
)

data class GoalResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("steps") val steps: List<StepResponse>, // Include steps
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class StepResponse(
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String
)

data class GoalStatusUpdateRequest(
    @SerializedName("status") val status: String
)

