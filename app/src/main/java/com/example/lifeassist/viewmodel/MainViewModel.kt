package com.example.lifeassist.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.model.Main
import com.example.lifeassist.model.Result
import com.example.lifeassist.repository.RepositoryProvider
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val userRepository = RepositoryProvider.userRepository

    private val _mainData = MutableLiveData<Main?>()
    val mainData: LiveData<Main?> = _mainData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        _isLoggedIn.value = true
    }

    fun fetchUserData(userId: String) {
        Log.d("MainViewModel", "fetchUserData called with userId=$userId")
        viewModelScope.launch {
            val result = userRepository.getUserData(userId)
            when (result) {
                is Result.Success -> {
                    Log.d("MainViewModel", "fetchUserData success, userId=${result.data.user.id}, goalsCount=${result.data.user.goals.size}")
                    // Always post user data so we can show username/description
                    _mainData.postValue(result.data)
                    if (result.data.user.goals.isEmpty()) {
                        _error.postValue("No goals available for this user.")
                    }
                }
                is Result.Error -> {
                    Log.e("MainViewModel", "fetchUserData error: ${result.message}")
                    _error.postValue(result.message)
                }
            }
        }
    }

    fun prepareAndSubmitGoal(userId: String, goalTitle: String, stepTitles: List<String>) {
        Log.d("MainViewModel", "prepareAndSubmitGoal called with userId=$userId, goalTitle=$goalTitle, stepCount=${stepTitles.size}")
        val steps = stepTitles.map {
            Main.Step(title = it, status = "pending")
        }
        val newGoal = Main.Goal(
            id = null,
            title = goalTitle,
            steps = steps,
            createdAt = null,
            updatedAt = null
        )
        submitGoal(userId, newGoal)
    }

    fun submitGoal(userId: String, goal: Main.Goal) {
        Log.d("MainViewModel", "submitGoal called with userId=$userId, goalId=${goal.id}, goalTitle=${goal.title}, stepsCount=${goal.steps.size}")
        viewModelScope.launch {
            val result = userRepository.submitGoal(userId, goal)
            when (result) {
                is Result.Success -> {
                    Log.d("MainViewModel", "submitGoal success, refreshing user data.")
                    fetchUserData(userId)
                }
                is Result.Error -> {
                    Log.e("MainViewModel", "submitGoal error: ${result.message}")
                    _error.postValue(result.message)
                }
            }
        }
    }

    fun updateGoal(goalId: String, updatedGoal: Main.Goal) {
        Log.d("MainViewModel", "updateGoal called with goalId=$goalId")
        _mainData.value?.let { main ->
            val updatedGoals = main.user.goals.map { goal ->
                if (goal.id == goalId) updatedGoal else goal
            }
            _mainData.postValue(main.copy(user = main.user.copy(goals = updatedGoals)))
        }
    }

    fun removeGoal(goalId: String) {
        Log.d("MainViewModel", "removeGoal called with goalId=$goalId")
        _mainData.value?.user?.let { user ->
            val filteredGoals = user.goals.filter { it.id != goalId }
            _mainData.postValue(_mainData.value?.copy(user = user.copy(goals = filteredGoals)))
        }
    }
}
