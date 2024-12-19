package com.example.lifeassist.model

data class Main(
    val user: User,                         // User associated with the goals
) {
    data class User(
        val id: String?,              // Unique user ID
        val username: String,            // User's name
        val email: String,           // User's email
        val description: String?,
        val goals: List<Goal>
    )

    data class Goal(
        val id: String?,                         // Unique goal ID
        val title: String,                      // Title of the goal
        val steps: List<Step>,
        var status: String?,// List of steps linked to the goal
        val createdAt: String?,                 // Creation timestamp
        val updatedAt: String?                  // Last update timestamp
    )

    data class Step(
        val title: String,          // Title of the step
        var status: String          // Status: "pending" or "completed"
    )
}
