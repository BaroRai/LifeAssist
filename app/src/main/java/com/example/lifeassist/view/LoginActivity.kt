package com.example.lifeassist.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lifeassist.databinding.ActivityLoginBinding
import com.example.lifeassist.viewmodel.LoginViewModel
import com.example.lifeassist.model.Result

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Navigate to RegisterActivity on click
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Handle login button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginViewModel.login(email, password)
        }
    }

    private fun observeViewModel() {
        loginViewModel.loginResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val user = result.data
                    saveLoginState(user.userId, user.email, binding.passwordInput.text.toString().trim())
                    navigateToMainActivity(user.username.toString())
                }
                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveLoginState(userId: String, email: String, password: String) {
        val sharedPreferences = getSharedPreferences("LifeAssistPrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("userId", userId)
            putString("userEmail", email)
            putString("userPassword", password)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    private fun navigateToMainActivity(username: String) {
        Toast.makeText(this, "Welcome $username", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
