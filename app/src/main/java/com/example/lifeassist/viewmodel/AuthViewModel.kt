package com.example.lifeassist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // LiveData to observe the AuthResponse (success message or error)
    private val _loginSuccess = MutableLiveData<String>()
    val loginSuccess: LiveData<String> = _loginSuccess

    // LiveData to handle errors
    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    // Function to perform login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Make the network request via the AuthRepository
                val response = authRepository.login(email, password)

                // Handle the successful response
                if (response != null) {
                    if (response.message != null) {
                        // Login successful, update LiveData with the success message
                        _loginSuccess.value = response.message!! // must be -Git error NPE
                    } else {
                        // If message is null, there's an error with the login
                        _loginError.value = "Error: ${response.error ?: "Unknown error"}"
                    }
                } else {
                    // If response is null or unsuccessful, show an error message
                    _loginError.value = "Login failed: Invalid credentials"
                }

            } catch (e: Exception) {
                // Handle network errors and other unexpected exceptions
                _loginError.value = "Error: ${e.message}"
            }
        }
    }
}
