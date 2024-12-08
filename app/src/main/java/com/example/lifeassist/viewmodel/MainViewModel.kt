package com.example.lifeassist.viewmodel

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
    val mainData: LiveData<Main?> get() = _mainData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    init {
        _isLoggedIn.value = true // Assume user is logged in
    }

    fun fetchUserData(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserData(userId)
            when (result) {
                is Result.Success -> _mainData.value = result.data
                is Result.Error -> _error.value = result.message
            }
        }
    }

    fun logoutUser() {
        _isLoggedIn.value = false
    }

    fun updateGoal(goalId: String, title: String, description: String?) {
        _mainData.value = _mainData.value?.let { main ->
            val updatedGoals = main.goals.map { goal ->
                if (goal.id == goalId) {
                    goal.copy(title = title, description = description)
                } else {
                    goal
                }
            }
            main.copy(goals = updatedGoals)
        }
    }

    fun addGoal(newGoal: Main.Goal) {
        _mainData.value = _mainData.value?.let { main ->
            main.copy(goals = main.goals + newGoal)
        }
    }

    fun updateSteps(goalId: String, newStep: Main.Step) {
        _mainData.value = _mainData.value?.let { main ->
            val updatedGoals = main.goals.map { goal ->
                if (goal.id == goalId) {
                    goal.copy(steps = goal.steps + newStep)
                } else {
                    goal
                }
            }
            main.copy(goals = updatedGoals)
        }
    }

    fun submitGoal(userId: String, goal: Main.Goal) {
        viewModelScope.launch {
            val result = userRepository.submitGoal(userId, goal)
            when (result) {
                is Result.Success -> fetchUserData(userId) // Refresh data on success
                is Result.Error -> _error.value = result.message
            }
        }
    }
}
