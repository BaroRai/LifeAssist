package com.example.lifeassist.model

data class Goal(
    val _id: String? = null, // Matches the API's unique identifier for the goal
    val title: String, // Title of the goal
    val description: String?, // Description of the goal
    val createdAt: String? = null, // Creation timestamp from the API
    val updatedAt: String? = null, // Last update timestamp from the API
    val steps: List<Step> = emptyList() // List of steps associated with the goal
)
