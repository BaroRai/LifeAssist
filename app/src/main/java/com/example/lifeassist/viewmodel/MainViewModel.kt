package com.example.lifeassist.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.api.RetrofitClient
import com.example.lifeassist.api.UserDataResponse
import com.example.lifeassist.model.Goal
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val apiService = RetrofitClient.authApi

    // LiveData to hold user data
    private val _userData = MutableLiveData<UserDataResponse>()
    val userData: LiveData<UserDataResponse> get() = _userData

    // LiveData to handle errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData for logged-in status
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    // Function to check if the user is logged in
    fun checkIfUserIsLoggedIn(context: Context) {
        val sharedPreferences = context.getSharedPreferences("LifeAssistPrefs", Context.MODE_PRIVATE)
        val loggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        _isLoggedIn.value = loggedIn
    }

    // Function to fetch user data from the API
    fun fetchUserData(email: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getUserData(email)
                if (response.isSuccessful) {
                    _userData.value = response.body()
                } else {
                    _error.value = "Failed to fetch user data"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    // Function to log out the user
    fun logoutUser(context: Context) {
        val sharedPreferences = context.getSharedPreferences("LifeAssistPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply() // Clear all session data
        _isLoggedIn.value = false  // Update the login status
    }

    // LiveData for popup visibility
    private val _isPopupVisible = MutableLiveData(false)
    val isPopupVisible: LiveData<Boolean> = _isPopupVisible

    // LiveData for popup animation properties
    private val _popupAlpha = MutableLiveData(0f)
    val popupAlpha: LiveData<Float> = _popupAlpha

    private val _popupTranslationY = MutableLiveData(100f)
    val popupTranslationY: LiveData<Float> = _popupTranslationY

    // Show the pop-up
    fun showPopup() {
        _isPopupVisible.value = true
        animatePopup(show = true)
    }

    // Hide the pop-up
    fun hidePopup() {
        animatePopup(show = false)
    }

    // Handle animation logic
    private fun animatePopup(show: Boolean) {
        if (show) {
            _popupAlpha.value = 1f
            _popupTranslationY.value = 0f
        } else {
            _popupAlpha.value = 0f
            _popupTranslationY.value = 100f
            _isPopupVisible.value = false
        }
    }

    private val _steps = MutableLiveData<MutableList<String>>(mutableListOf())
    val steps: LiveData<MutableList<String>> = _steps

    private val _goalSubmissionStatus = MutableLiveData<String>()
    val goalSubmissionStatus: LiveData<String> = _goalSubmissionStatus

    fun addStep(step: String) {
        _steps.value?.add(step)
        _steps.value = _steps.value // Trigger LiveData update
    }

    fun getSteps(): List<String> {
        return _steps.value ?: emptyList()
    }

    fun prepareGoalData(userId: String, goalTitle: String): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "goalTitle" to goalTitle,
            "steps" to getSteps()
        )
    }

    fun submitGoal(userId: String, goalTitle: String) {
        val goalData = prepareGoalData(userId, goalTitle)
        // Simulate API submission or prepare for database insertion
        Log.d("MainViewModel", "Goal data prepared for database: $goalData")
        _goalSubmissionStatus.value = "Goal submitted with ${goalData["steps"].toString().count()} steps!"
    }
}
