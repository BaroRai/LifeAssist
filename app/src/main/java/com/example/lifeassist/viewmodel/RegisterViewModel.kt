package com.example.lifeassist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.api.AuthResponse
import com.example.lifeassist.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()

    // LiveData to observe the registration result (AuthResponse)
    private val _registerResult = MutableLiveData<AuthResponse>()
    val registerResult: LiveData<AuthResponse> = _registerResult

    // LiveData to handle errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Function to perform registration
    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Call register function in repository, which returns AuthResponse?
                val response = repository.register(email, password)

                // Check if the response is not null (successful registration)
                if (response != null) {
                    _registerResult.value = response  // Set the registration result
                } else {
                    // If response is null, set the error message
                    _error.value = "Registration failed: Invalid credentials or other error"
                }
            } catch (e: Exception) {
                // Catch any errors and update the error LiveData
                _error.value = "Network error: ${e.message}"
            }
        }
    }
}
