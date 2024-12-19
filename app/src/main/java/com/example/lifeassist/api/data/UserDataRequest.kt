package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class UserDataRequest(
    @SerializedName("userId") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("description") val description: String?,
    @SerializedName("goals") val goals: List<GoalRequest> // Goals to be sent
)

data class GoalRequest(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("steps") val steps: List<StepRequest>,// Steps associated with the goal
    @SerializedName("status") val status: String?
)

data class StepRequest(
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String = "pending" // Default to "pending"
)
