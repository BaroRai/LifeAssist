package com.example.lifeassist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeassist.model.Result
import com.example.lifeassist.repository.RepositoryProvider
import kotlinx.coroutines.launch

class RegisterViewModel() : ViewModel() {
    private val authRepository = RepositoryProvider.authRepository

    private val _registrationResult = MutableLiveData<Result<Unit>>()
    val registrationResult: LiveData<Result<Unit>> get() = _registrationResult

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.register(email, password)
            _registrationResult.value = result
        }
    }
}

