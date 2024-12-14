package com.example.lifeassist.repository

import android.util.Log
import com.example.lifeassist.api.interfaces.ApiService
import com.example.lifeassist.model.Main
import com.example.lifeassist.model.Result
import com.example.lifeassist.api.data.GoalRequest
import com.example.lifeassist.api.data.StepRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val apiService: ApiService) {

    suspend fun getUserData(userId: String): Result<Main> = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "getUserData called with userId=$userId")
        try {
            val response = apiService.getUserData(userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("UserRepository", "getUserData success: username=${body.username}, goals=${body.goals.size}")
                Result.Success(mapResponseToMain(body))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("UserRepository", "getUserData error: $errorBody")
                Result.Error("Error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "getUserData exception: ${e.localizedMessage}", e)
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun submitGoal(userId: String, goal: Main.Goal): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "submitGoal called with userId=$userId, goalId=${goal.id}, goalTitle=${goal.title}")
        try {
            val goalRequest = mapGoalToRequest(goal)
            Log.d("UserRepository", "submitGoal sending request: $goalRequest")
            val response = apiService.submitGoal(userId, goalRequest)
            if (response.isSuccessful) {
                Log.d("UserRepository", "submitGoal success")
                Result.Success(Unit)
            } else {
                Log.e("UserRepository", "submitGoal failed: ${response.message()}")
                Result.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "submitGoal exception: ${e.localizedMessage}", e)
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    private fun mapResponseToMain(response: com.example.lifeassist.api.data.UserDataResponse): Main {
        return Main(
            user = Main.User(
                id = response.userId,
                username = response.username,
                email = response.email,
                description = response.description,
                goals = response.goals.map { goal ->
                    Main.Goal(
                        id = goal.id,
                        title = goal.title,
                        steps = goal.steps.map { step ->
                            Main.Step(
                                title = step.title,
                                status = step.status
                            )
                        },
                        status = goal.status,
                        createdAt = goal.createdAt,
                        updatedAt = goal.updatedAt
                    )
                }
            )
        )
    }

    private fun mapGoalToRequest(goal: Main.Goal): GoalRequest {
        return GoalRequest(
            id = goal.id.toString(),
            title = goal.title,
            steps = goal.steps.map { step ->
                StepRequest(title = step.title, status = step.status)
            }
        )
    }
}
