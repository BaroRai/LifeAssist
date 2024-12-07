package com.example.lifeassist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.repository.AuthRepository
import com.example.lifeassist.model.User
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    // New LiveData to hold a User object on successful login
    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> get() = _loginError

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.login(email, password)
                if (response != null) {
                    if (response.message == "Login successful" && !response.userId.isNullOrEmpty()) {
                        // Create a User object and set it
                        val user = User(
                            userId = response.userId,
                            email = email,
                            password = password
                        )
                        _loggedInUser.value = user
                    } else {
                        _loginError.value = response.error ?: "Login failed"
                    }
                } else {
                    _loginError.value = "Login failed: Invalid credentials"
                }
            } catch (e: Exception) {
                _loginError.value = "Network error: ${e.message}"
            }
        }
    }
}

