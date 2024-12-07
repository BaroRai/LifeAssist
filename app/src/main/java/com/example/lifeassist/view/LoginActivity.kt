package com.example.lifeassist.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lifeassist.databinding.ActivityLoginBinding
import com.example.lifeassist.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerLink.setOnClickListener {
            Log.d("LoginActivity", "Register link clicked")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Trigger the login function in the ViewModel
            authViewModel.login(email, password)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        // Observe the loggedInUser LiveData
        authViewModel.loggedInUser.observe(this) { user ->
            user?.let {
                // Show success message
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // Store the userId and userEmail in SharedPreferences
                val sharedPreferences = getSharedPreferences("LifeAssistPrefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("userId", it.userId)
                    putString("userEmail", it.email)
                    putBoolean("isLoggedIn", true)
                    apply()
                }

                // Redirect to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Finish LoginActivity
            }
        }

        // Observe login error
        authViewModel.loginError.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
