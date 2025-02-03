package com.example.lifeassist.utils

import android.content.Context
import android.util.Log

object SharedPreferencesHelper {

    private const val PREFS_NAME = "LifeAssistPrefs"
    private const val TAG = "SharedPreferencesHelper"

    // Retrieve user ID
    fun getUserId(context: Context): String {
        val userId = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("userId", null)
        Log.d(TAG, "getUserId returned: $userId")
        return userId ?: ""
    }

    // Retrieve username
    fun getUsername(context: Context): String? {
        val username = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("username", null)
        Log.d(TAG, "getUsername returned: $username")
        return username
    }

    // Retrieve email
    fun getEmail(context: Context): String? {
        val email = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("email", null)
        Log.d(TAG, "getEmail returned: $email")
        return email
    }

    // Retrieve description
    fun getDescription(context: Context): String? {
        val description = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("description", null)
        Log.d(TAG, "getDescription returned: $description")
        return description
    }

    // Save user data
    fun saveUserData(context: Context, userId: String?, username: String?, email: String?, description: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("userId", userId ?: "")
            putString("username", username ?: "")
            putString("email", email ?: "")
            putString("description", description ?: "")
            apply()
        }
        Log.d(TAG, "saveUserData: Saved userId=$userId, username=$username, email=$email, description=$description")
    }

    // Save only user ID
    fun saveUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("userId", userId).apply()
        Log.d(TAG, "saveUserId: Saved userId=$userId")
    }

    // Save username
    fun saveUsername(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("username", username).apply()
        Log.d(TAG, "saveUsername: Saved username=$username")
    }

    // Save email
    fun saveEmail(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("email", email).apply()
        Log.d(TAG, "saveEmail: Saved email=$email")
    }

    // Save description
    fun saveDescription(context: Context, description: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("description", description).apply()
        Log.d(TAG, "saveDescription: Saved description=$description")
    }

    // Clear all user data
    fun clearUserData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d(TAG, "clearUserData: Cleared all user data")
    }

    // Log current SharedPreferences state (useful for debugging)
    fun logCurrentState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "Current SharedPreferences state: ${prefs.all}")
    }

    //Password methods
    fun getPassword(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("userPassword", null)

    fun savePassword(context: Context, password: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("userPassword", password)
            .apply()
    }

}
