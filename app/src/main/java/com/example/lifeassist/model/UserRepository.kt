package com.example.lifeassist.model

import android.content.Context

class UserRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)

    // Save user credentials
    fun registerUser(user: User): Boolean {
        return if (sharedPreferences.contains(user.email)) {
            false // User already exists
        } else {
            sharedPreferences.edit()
                .putString(user.email, user.password)
                .apply()
            true
        }
    }

    // Validate login credentials
    fun loginUser(email: String, password: String): Boolean {
        val storedPassword = sharedPreferences.getString(email, null)
        return storedPassword == password
    }
}
