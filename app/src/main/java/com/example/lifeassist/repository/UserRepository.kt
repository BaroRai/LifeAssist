package com.example.lifeassist.repository

import android.content.Context
import android.util.Log
import com.example.lifeassist.api.interfaces.ApiService
import com.example.lifeassist.model.Main
import com.example.lifeassist.model.Result
import com.example.lifeassist.api.data.GoalRequest
import com.example.lifeassist.api.data.GoalStatusUpdateRequest
import com.example.lifeassist.api.data.StepRequest
import com.example.lifeassist.api.data.UserDataResponse
import com.example.lifeassist.utils.SharedPreferencesHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val apiService: ApiService) {

    suspend fun getUserData(context: Context, userId: String): Result<Main> = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "getUserData called with userId=$userId")
        try {
            val response = apiService.getUserData(userId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("UserRepository", "getUserData success: username=${body.username}, goals=${body.goals.size}")

                Result.Success(mapResponseToMain(context, body)) // Pass context and response
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

    private fun mapResponseToMain(context: Context, response: UserDataResponse): Main {
        val userId = response.userId ?: SharedPreferencesHelper.getUserId(context)

        if (userId.isEmpty()) {
            throw IllegalStateException("User ID is missing both in the API response and SharedPreferences")
        }

        return Main(
            user = Main.User(
                id = userId,
                username = response.username.orEmpty(),
                email = response.email.orEmpty(),
                description = response.description.orEmpty(),
                goals = response.goals?.map { goal ->
                    Main.Goal(
                        id = goal.id.orEmpty(),
                        title = goal.title.orEmpty(),
                        steps = goal.steps?.map { step ->
                            Main.Step(
                                title = step.title.orEmpty(),
                                status = step.status.orEmpty()
                            )
                        } ?: emptyList(),
                        status = goal.status.orEmpty(),
                        createdAt = goal.createdAt.orEmpty(),
                        updatedAt = goal.updatedAt.orEmpty()
                    )
                } ?: emptyList()
            )
        )
    }

    private fun mapGoalToRequest(goal: Main.Goal): GoalRequest {
        val goalRequest = GoalRequest(
            id = goal.id.toString(),
            title = goal.title,
            steps = goal.steps.map { step ->
                StepRequest(title = step.title, status = step.status)
            },
            status = goal.status
        )
        Log.d("UserRepository", "Mapped GoalRequest: ${Gson().toJson(goalRequest)}")
        return goalRequest
    }


    suspend fun updateGoalStatus(userId: String, goalId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "updateGoalStatus called with userId=$userId, goalId=$goalId, status=$status")
        try {
            val request = GoalStatusUpdateRequest(status)
            Log.d("UserRepository", "updateGoalStatus sending request: $request")
            val response = apiService.updateGoalStatus(userId, goalId, request)
            if (response.isSuccessful) {
                Log.d("UserRepository", "updateGoalStatus success")
                Result.Success(Unit)
            } else {
                Log.e("UserRepository", "updateGoalStatus failed: ${response.message()}")
                Result.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "updateGoalStatus exception: ${e.localizedMessage}", e)
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }
}
