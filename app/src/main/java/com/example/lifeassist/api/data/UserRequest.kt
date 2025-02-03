package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("userId") val id: String
)

data class UpdateProfileRequest(
    @SerializedName("userId") val id: String? = null,
    @SerializedName("description") val description: String? = null,
    val username: String
)

data class UpdateDescriptionRequest(
    @SerializedName("description") val description: String,
)