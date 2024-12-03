package com.example.lifeassist.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.api.RetrofitClient
import com.example.lifeassist.api.UserDataResponse
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
}
