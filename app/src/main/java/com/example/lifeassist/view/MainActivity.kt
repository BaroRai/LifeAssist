package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lifeassist.R
import com.example.lifeassist.databinding.ActivityMainBinding
import com.example.lifeassist.databinding.PopupGoalBinding
import com.example.lifeassist.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var popupBinding: PopupGoalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user is logged in
        mainViewModel.checkIfUserIsLoggedIn(this)

        // Observe the logged-in status from ViewModel
        mainViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (!isLoggedIn) {
                redirectToLogin()
            }
        }

        val sharedPreferences = getSharedPreferences("LifeAssistPrefs", MODE_PRIVATE)
        val email = sharedPreferences.getString("userEmail", null)

        if (email.isNullOrEmpty()){
            redirectToLogin()
        } else {
            mainViewModel.fetchUserData(email)
        }

        // Observe user data from ViewModel
        mainViewModel.userData.observe(this) { userData ->
            // Update UI with user data
            userData?.let {
                binding.usernameTextView.text = it.username
                binding.descriptionTextView.text = it.description
            }
        }

        // Observe errors from ViewModel
        mainViewModel.error.observe(this) { errorMessage ->
            Log.e("MainActivity", errorMessage)
        }

        // Handle Logout Button Click
        binding.navLogout.setOnClickListener {
            Log.d("Logout", "I was clicked! (logout)")
            mainViewModel.logoutUser(this)
            redirectToLogin()
        }

        // Handle Drawer Open/Close Logic
        drawerLayout = findViewById(R.id.drawer_layout)
        binding.container.setOnClickListener {
            if (drawerLayout.isDrawerOpen(binding.navDrawer)) {
                drawerLayout.closeDrawer(binding.navDrawer)
            } else {
                drawerLayout.openDrawer(binding.navDrawer)
            }
        }

        // Navigation menu items
        binding.navHome.setOnClickListener { /* Home logic */ }
        binding.navProfile.setOnClickListener { /* Profile logic */ }
        binding.navSettings.setOnClickListener { /* Settings logic */ }

        // Inflate the popup layout
        val popupView = layoutInflater.inflate(R.layout.popup_goal, binding.container, false)
        popupBinding = PopupGoalBinding.bind(popupView)
        binding.container.addView(popupView)
        popupBinding.root.visibility = View.GONE

        // Show the popup on Add Goal button click
        binding.openPopupButton.setOnClickListener {
            Log.d("MainActivity", "Open Popup Button Clicked")
            popupBinding.root.visibility = View.VISIBLE
        }

        // Close the popup
        popupBinding.closeButton.setOnClickListener {
            // Just hide the popup instead of clearing everything
            popupBinding.root.visibility = View.GONE
        }

        // Utility function for dp to px
        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Add Step Button logic
        popupBinding.addStepButton.setOnClickListener {
            val stepText = popupBinding.stepInput.text.toString()

            if (stepText.isBlank()) {
                Toast.makeText(this, "Step cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val stepsLayout = popupBinding.stepsLayout
                    ?: throw IllegalStateException("Steps layout is not properly initialized")

                // Create a horizontal layout for the step
                val stepLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 16, 0, 0) // margin between steps
                    }
                }

                // Create the step EditText
                val stepEditText = EditText(this).apply {
                    setText(stepText)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        setMargins(8, 0, 8, 0)
                    }
                    isEnabled = false
                }

                // Toggle button for editing
                val toggleEditButton = Button(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        48.dpToPx(this@MainActivity),
                        48.dpToPx(this@MainActivity)
                    )
                    background = getDrawable(R.drawable.ic_baseline_edit_24)
                    setOnClickListener {
                        if (stepEditText.isEnabled) {
                            stepEditText.isEnabled = false
                            background = getDrawable(R.drawable.ic_baseline_edit_24)
                        } else {
                            stepEditText.isEnabled = true
                            stepEditText.requestFocus()
                            background = getDrawable(R.drawable.ic_baseline_deny_24)
                        }
                    }
                }

                // Add views to stepLayout
                stepLayout.addView(stepEditText)
                stepLayout.addView(toggleEditButton)

                // Add the new step at the end of stepsLayout
                stepsLayout.addView(stepLayout)

                // Clear the input field for the next step
                popupBinding.stepInput.text.clear()

            } catch (e: Exception) {
                Log.e("MainActivity", "Error adding step: ${e.message}")
                Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Accept Button logic
        popupBinding.acceptButton.setOnClickListener {
            val goalTitle = popupBinding.goalInput.text.toString().trim()
            if (goalTitle.isNotBlank()) {
                val userId = sharedPreferences.getString("userId", null)

                if (!userId.isNullOrEmpty()) {
                    mainViewModel.submitGoal(userId, goalTitle)
                    Toast.makeText(this, "Goal submitted successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                }

                // Hide the popup
                popupBinding.root.visibility = View.GONE

            } else {
                Toast.makeText(this, "Goal title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
