package com.example.lifeassist.repository

import com.example.lifeassist.api.interfaces.ApiService
import com.example.lifeassist.model.Main
import com.example.lifeassist.model.Result
import com.example.lifeassist.api.data.*

class UserRepository(private val apiService: ApiService) {

    suspend fun getUserData(userId: String): Result<Main> {
        return try {
            val response = apiService.getUserData(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(mapResponseToMain(it))
                } ?: Result.Error("Empty response from server")
            } else {
                Result.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    private fun mapResponseToMain(response: UserDataResponse): Main {
        return Main(
            user = Main.User(
                id = response.userId,
                name = response.username,
                email = response.email
            ),
            goals = response.goals.map { goal ->
                Main.Goal(
                    id = goal.id,
                    title = goal.title,
                    description = goal.description,
                    steps = goal.steps.map { step ->
                        Main.Step(
                            id = step.id,
                            title = step.title,
                            status = step.status
                        )
                    },
                    createdAt = goal.createdAt,
                    updatedAt = goal.updatedAt
                )
            }
        )
    }

    suspend fun submitGoal(userId: String, goal: Main.Goal): Result<Unit> {
        return try {
            val goalRequest = mapGoalToRequest(goal)
            val response = apiService.submitGoal(userId, goalRequest)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    private fun mapGoalToRequest(goal: Main.Goal): GoalRequest {
        return GoalRequest(
            title = goal.title,
            description = goal.description,
            steps = goal.steps.map { step ->
                StepRequest(title = step.title, status = step.status)
            }
        )
    }
}
