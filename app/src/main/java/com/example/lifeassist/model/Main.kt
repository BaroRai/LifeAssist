package com.example.lifeassist.model

data class Main(
    val user: User,                 // User associated with the goals
    val goals: List<Goal> = emptyList() // List of goals for the user
) {
    data class User(
        val id: String,             // Unique user ID
        val name: String,           // User's name
        val email: String           // User's email
    )

    data class Goal(
        val id: String,             // Unique goal ID
        val title: String,          // Title of the goal
        val description: String?,   // Description of the goal
        val steps: List<Step> = emptyList(), // List of steps linked to the goal
        val createdAt: String?,     // Creation timestamp
        val updatedAt: String?      // Last update timestamp
    )

    data class Step(
        val id: String,             // Unique step ID
        val title: String,          // Title of the step
        val status: String          // Status: "pending" or "completed"
    )
}
