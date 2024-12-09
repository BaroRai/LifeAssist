package com.example.lifeassist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.model.Login
import com.example.lifeassist.model.Result
import com.example.lifeassist.repository.RepositoryProvider
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authRepository = RepositoryProvider.authRepository

    private val _loginResult = MutableLiveData<Result<Login>>()
    val loginResult: LiveData<Result<Login>> get() = _loginResult

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            when (result) {
                is Result.Success -> {
                    _loginResult.value = result
                    _isLoggedIn.value = true // Update login status
                }
                is Result.Error -> {
                    _loginResult.value = result
                    _isLoggedIn.value = false // Update login status
                }
            }
        }
    }
}
