package com.example.lifeassist.repository

import com.example.lifeassist.api.RetrofitClient

object RepositoryProvider {
    val userRepository: UserRepository by lazy {
        UserRepository(RetrofitClient.apiService)
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }
}
