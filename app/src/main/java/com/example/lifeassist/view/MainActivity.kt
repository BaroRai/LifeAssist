package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lifeassist.R
import com.example.lifeassist.databinding.ActivityMainBinding
import com.example.lifeassist.databinding.PopupGoalBinding
import com.example.lifeassist.model.Main
import com.example.lifeassist.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var popupBinding: PopupGoalBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupPopup()
        setupDrawer()

        val userId = getSharedPreferences("LifeAssistPrefs", Context.MODE_PRIVATE)
            .getString("userId", null)

        if (userId.isNullOrEmpty()) {
            redirectToLogin()
        } else {
            mainViewModel.fetchUserData(userId)
        }

        setupNavigationMenu()
    }

    private fun setupObservers() {
        mainViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (!isLoggedIn) redirectToLogin()
        }

        mainViewModel.mainData.observe(this) { mainData ->
            mainData?.let { data ->
                // Update UI with user information
                binding.usernameTextView.text = data.user.name // Display username
                binding.descriptionTextView.text = if (data.user.description.isNullOrEmpty()) {
                    "No description available" // Handle empty description case
                } else {
                    data.user.description // Display user description
                }
            }
        }

        mainViewModel.mainData.observe(this) { mainData ->
            mainData?.let { data ->
                // Update UI with user and goals
                binding.usernameTextView.text = data.user.name
                binding.descriptionTextView.text = if (data.goals.isNotEmpty()) {
                    "Current Goal: ${data.goals[0].title}"
                } else {
                    "No goals available"
                }
            }
        }

        mainViewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupPopup() {
        popupBinding = PopupGoalBinding.inflate(layoutInflater)
        binding.container.addView(popupBinding.root)
        popupBinding.root.visibility = View.GONE

        // Show popup for adding goals
        binding.openPopupButton.setOnClickListener {
            popupBinding.root.visibility = View.VISIBLE
            resetPopup() // Reset only the necessary parts
        }

        // Close popup
        popupBinding.closeButton.setOnClickListener {
            popupBinding.root.visibility = View.GONE
        }

        // Add step dynamically
        popupBinding.addStepButton.setOnClickListener {
            val stepText = popupBinding.stepInput.text.toString().trim()
            if (stepText.isEmpty()) {
                Toast.makeText(this, "Step cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Dynamically create a container for the step
            val stepContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8, 8, 8, 8)
            }

            // Create EditText for the step
            val stepEditText = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setText(stepText)
                isEnabled = false // Disable editing by default
            }

            // Create Edit Button
            val editButton = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageResource(R.drawable.ic_baseline_edit_24)
                contentDescription = "Edit Step"
            }

            // Create Delete Button
            val deleteButton = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageResource(R.drawable.ic_baseline_deny_24)
                contentDescription = "Delete Step"
            }

            // Add buttons and EditText to the container
            stepContainer.addView(stepEditText)
            stepContainer.addView(editButton)
            stepContainer.addView(deleteButton)

            // Add the container to steps layout
            popupBinding.stepsContainer.addView(stepContainer)

            // Set click listener for delete button
            deleteButton.setOnClickListener {
                popupBinding.stepsContainer.removeView(stepContainer)
            }

            // Set click listener for edit button
            editButton.setOnClickListener {
                if (stepEditText.isEnabled) {
                    // If already enabled, stop editing
                    stepEditText.isEnabled = false
                    editButton.setImageResource(R.drawable.ic_baseline_edit_24) // Change back to edit icon
                } else {
                    // Enable editing
                    stepEditText.isEnabled = true
                    stepEditText.requestFocus()
                    editButton.setImageResource(R.drawable.ic_baseline_stop_edit_24) // Change to a checkmark icon
                }
            }

            // Clear the input field
            popupBinding.stepInput.text.clear()
        }

        // Accept and save the goal
        popupBinding.acceptButton.setOnClickListener {
            val goalTitle = popupBinding.goalInput.text.toString().trim()
            if (goalTitle.isEmpty()) {
                Toast.makeText(this, "Goal title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val steps = mutableListOf<Main.Step>()
            for (i in 0 until popupBinding.stepsContainer.childCount) {
                val stepContainer = popupBinding.stepsContainer.getChildAt(i) as LinearLayout
                val stepEditText = stepContainer.getChildAt(0) as EditText
                val stepTitle = stepEditText.text.toString().trim()
                if (stepTitle.isNotEmpty()) {
                    steps.add(Main.Step("step_${System.currentTimeMillis()}_$i", stepTitle, "pending"))
                }
            }

            val newGoal = Main.Goal(
                id = "goal_${System.currentTimeMillis()}",
                title = goalTitle,
                description = null,
                steps = steps,
                createdAt = null,
                updatedAt = null
            )

            mainViewModel.submitGoal("userIdPlaceholder", newGoal)

            // Hide popup and reset necessary fields
            popupBinding.root.visibility = View.GONE
            resetPopup()
        }
    }

    private fun resetPopup() {
        // Remove all dynamically added steps
        popupBinding.goalInput.text.clear()
        popupBinding.stepsContainer.removeAllViews()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val hamburgerButton = findViewById<ImageButton>(R.id.hamburger_menu_button)

        // Open Drawer on Hamburger Button Click
        hamburgerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }

    private fun setupNavigationMenu() {
        // Handle Home click
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Handle Profile click
        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Handle Settings click
        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Handle Logout click
        findViewById<TextView>(R.id.nav_logout).setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Clear session data
        val sharedPreferences = getSharedPreferences("LifeAssistPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
