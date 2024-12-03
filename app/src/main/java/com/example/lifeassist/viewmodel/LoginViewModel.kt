package com.example.lifeassist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.api.AuthResponse
import com.example.lifeassist.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // LiveData to observe the AuthResponse (success message or error)
    private val _loginResult = MutableLiveData<AuthResponse?>()
    val loginResult: LiveData<AuthResponse?> = _loginResult

    // LiveData to handle errors
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Function to perform login
    fun login(email: String, password: String) {
        // Launch coroutine to handle network call
        viewModelScope.launch {
            try {
                val authResponse = authRepository.login(email, password)
                if (authResponse != null) {
                    _loginResult.value = authResponse // Success response
                } else {
                    _error.value = "Login failed: Invalid credentials"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}" // Handle any network errors
            }
        }
    }
}
