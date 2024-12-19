package com.example.lifeassist.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.model.Main
import com.example.lifeassist.model.Result
import com.example.lifeassist.repository.RepositoryProvider
import com.example.lifeassist.utils.SharedPreferencesHelper
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val userRepository = RepositoryProvider.userRepository

    private val _mainData = MutableLiveData<Main?>()
    val mainData: LiveData<Main?> = _mainData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    val goalsLiveData = MutableLiveData<List<Main.Goal>>()

    init {
        _isLoggedIn.value = true
    }

    fun fetchUserData(context: Context, saveToPrefs: Boolean = true) {
        val userId = SharedPreferencesHelper.getUserId(context)
        if (userId.isEmpty()) {
            _isLoggedIn.postValue(false)
            _error.postValue("User not logged in.")
            return
        }

        viewModelScope.launch {
            val result = userRepository.getUserData(context, userId)
            when (result) {
                is Result.Success -> {
                    val userData = result.data.user
                    Log.d("MainViewModel", "Fetched user data: $userData")

                    if (saveToPrefs) {
                        SharedPreferencesHelper.saveUserData(
                            context,
                            userId = userData.id,
                            username = userData.username,
                            email = userData.email,
                            description = userData.description
                        )
                    }

                    _mainData.postValue(result.data)
                }
                is Result.Error -> {
                    Log.e("MainViewModel", "Error fetching user data: ${result.message}")
                    _error.postValue(result.message)
                }
            }
        }
    }

    fun initializeUserData(context: Context) {
        val userId = SharedPreferencesHelper.getUserId(context)
        val username = SharedPreferencesHelper.getUsername(context).orEmpty()
        val email = SharedPreferencesHelper.getEmail(context).orEmpty()
        val description = SharedPreferencesHelper.getDescription(context).orEmpty()

        if (userId.isEmpty()) {
            Log.e("MainViewModel", "initializeUserData: User ID is missing. Redirecting to login.")
            _isLoggedIn.postValue(false)
            _error.postValue("User not logged in.")
            return
        }

        // Log current state
        Log.d("MainViewModel", "User data from SharedPreferences: userId=$userId, username=$username, email=$email, description=$description")

        if (username.isEmpty() || email.isEmpty() || description.isEmpty()) {
            Log.d("MainViewModel", "Incomplete user data in SharedPreferences, fetching from backend.")
            fetchUserData(context, saveToPrefs = true)
        } else {
            Log.d("MainViewModel", "Using data from SharedPreferences to initialize.")
            _mainData.postValue(
                Main(
                    user = Main.User(
                        id = userId,
                        username = username,
                        email = email,
                        description = description,
                        goals = emptyList()
                    )
                )
            )
        }
    }

    fun prepareAndSubmitGoal(context: Context, goalTitle: String, stepTitles: List<String>) {
        val userId = SharedPreferencesHelper.getUserId(context)
        if (userId.isBlank()) { // Check for an empty string instead of null
            Log.e("MainViewModel", "prepareAndSubmitGoal: User ID is missing.")
            _error.postValue("User not logged in.")
            return
        }

        Log.d("MainViewModel", "prepareAndSubmitGoal called with userId=$userId, goalTitle=$goalTitle, stepCount=${stepTitles.size}")
        val steps = stepTitles.map {
            Main.Step(title = it, status = "pending")
        }
        val newGoal = Main.Goal(
            id = null,
            title = goalTitle,
            steps = steps,
            status = "pending",
            createdAt = null,
            updatedAt = null
        )
        Log.d("MainViewModel", "Constructed newGoal: $newGoal")
        submitGoal(context, userId, newGoal)
    }

    private fun submitGoal(context: Context, userId: String, goal: Main.Goal) {
        Log.d("MainViewModel", "submitGoal called with userId=$userId, goalId=${goal.id}, goalTitle=${goal.title}, stepsCount=${goal.steps.size}")
        viewModelScope.launch {
            val result = userRepository.submitGoal(userId, goal)
            when (result) {
                is Result.Success -> {
                    Log.d("MainViewModel", "submitGoal success, refreshing user data.")
                    fetchUserData(context)
                }
                is Result.Error -> {
                    Log.e("MainViewModel", "submitGoal error: ${result.message}")
                    _error.postValue("Failed to submit goal: ${result.message}")
                    Toast.makeText(context, "Goal submission failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun prepareAndUpdateGoalStatus(context: Context, goalId: String?, newStatus: String) {
        val userId = SharedPreferencesHelper.getUserId(context)
        if (userId.isEmpty()) {
            Log.e("MainViewModel", "prepareAndUpdateGoalStatus: User ID is null or empty.")
            _error.postValue("User not logged in.")
            return
        }

        if (goalId.isNullOrEmpty()) {
            Log.e("MainViewModel", "prepareAndUpdateGoalStatus: Goal ID is null or empty.")
            _error.postValue("Invalid goal ID.")
            return
        }

        val goal = findGoalById(goalId)
        if (goal == null) {
            Log.e("MainViewModel", "prepareAndUpdateGoalStatus: Goal not found in mainData.")
            _error.postValue("Goal not found.")
            return
        }

        Log.d("MainViewModel", "prepareAndUpdateGoalStatus called with userId=$userId, goalId=$goalId, newStatus=$newStatus")
        viewModelScope.launch {
            val result = userRepository.updateGoalStatus(userId, goalId, newStatus)
            when (result) {
                is Result.Success -> {
                    Log.d("MainViewModel", "Goal status updated successfully.")
                    fetchUserData(context) // Refresh data with context
                }
                is Result.Error -> {
                    Log.e("MainViewModel", "Error updating goal status: ${result.message}")
                    _error.postValue(result.message)
                }
            }
        }
    }

    private fun findGoalById(goalId: String?): Main.Goal? {
        if (goalId.isNullOrEmpty()) {
            Log.e("MainViewModel", "findGoalById: Goal ID is null or empty")
            return null
        }

        val goals = mainData.value?.user?.goals
        if (goals.isNullOrEmpty()) {
            Log.e("MainViewModel", "findGoalById: No goals available in mainData")
            return null
        }

        val goal = goals.find { it.id == goalId }
        if (goal == null) {
            Log.e("MainViewModel", "findGoalById: Goal with ID=$goalId not found")
        } else {
            Log.d("MainViewModel", "findGoalById: Found goal with ID=$goalId: $goal")
        }
        return goal
    }


    fun logout(context: Context) {
        Log.d("MainViewModel", "logout: Clearing user data.")
        SharedPreferencesHelper.clearUserData(context)
        _isLoggedIn.postValue(false) // Notify activity to redirect to login
    }
}
