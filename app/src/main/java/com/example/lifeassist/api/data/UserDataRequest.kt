package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class UserDataRequest(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("description") val description: String?,
    @SerializedName("goals") val goals: List<GoalRequest> // Goals to be sent
)

data class GoalRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("steps") val steps: List<StepRequest> // Steps associated with the goal
)

data class StepRequest(
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String = "pending" // Default to "pending"
)
