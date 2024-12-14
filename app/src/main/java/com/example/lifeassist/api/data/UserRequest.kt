package com.example.lifeassist.api.data

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("userId") val id: String
)