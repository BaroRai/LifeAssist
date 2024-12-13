package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        val userId = getUserId()
        Log.d("MainActivity", "Retrieved userId from SharedPreferences: $userId")

        if (userId.isNullOrEmpty()) {
            Log.d("MainActivity", "No userId found, redirecting to login.")
            redirectToLogin()
        } else {
            Log.d("MainActivity", "Calling fetchUserData with userId: $userId")
            mainViewModel.fetchUserData(userId)
        }

        setupNavigationMenu()
    }

    private fun setupObservers() {
        mainViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
            if (!isLoggedIn) redirectToLogin()
        }

        mainViewModel.mainData.observe(this) { mainData ->
            mainData?.let { data ->
                // Display username and description
                binding.usernameTextViewDrawer.text = data.user.username
                val descriptionText = data.user.description ?: "No description available"
                binding.descriptionTextView.text = descriptionText

                // Display goals in goalsList LinearLayout
                val goalsList = binding.goalsList
                goalsList.removeAllViews() // clear old views
                data.user.goals.forEach { goal ->
                    val goalTextView = TextView(this).apply {
                        text = "Goal: ${goal.title}"
                        textSize = 16f
                        setPadding(16,16,16,16)
                    }
                    goalsList.addView(goalTextView)

                    // If you want to display steps:
                    goal.steps.forEach { step ->
                        val stepTextView = TextView(this).apply {
                            text = " - Step: ${step.title} (${step.status})"
                            textSize = 14f
                            setPadding(32,8,16,8)
                        }
                        goalsList.addView(stepTextView)
                    }
                }
            }
        }

        mainViewModel.error.observe(this) { error ->
            Log.e("MainActivity", "Error observed: $error")
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
            resetPopup()
        }

        // Close popup
        popupBinding.closeButton.setOnClickListener {
            popupBinding.root.visibility = View.GONE
        }

        // Add steps dynamically
        popupBinding.addStepButton.setOnClickListener {
            val stepText = popupBinding.stepInput.text.toString().trim()
            if (stepText.isEmpty()) {
                Toast.makeText(this, "Step cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stepContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(8, 8, 8, 8)
            }

            val stepEditText = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setText(stepText)
                isEnabled = false
            }

            val editButton = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageResource(R.drawable.ic_baseline_edit_24)
                contentDescription = "Edit Step"
            }

            val deleteButton = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setImageResource(R.drawable.ic_baseline_deny_24)
                contentDescription = "Delete Step"
            }

            stepContainer.addView(stepEditText)
            stepContainer.addView(editButton)
            stepContainer.addView(deleteButton)

            popupBinding.stepsContainer.addView(stepContainer)

            deleteButton.setOnClickListener {
                popupBinding.stepsContainer.removeView(stepContainer)
            }

            editButton.setOnClickListener {
                if (stepEditText.isEnabled) {
                    stepEditText.isEnabled = false
                    editButton.setImageResource(R.drawable.ic_baseline_edit_24)
                } else {
                    stepEditText.isEnabled = true
                    stepEditText.requestFocus()
                    editButton.setImageResource(R.drawable.ic_baseline_stop_edit_24)
                }
            }

            popupBinding.stepInput.text.clear()
        }

        // Accept and save the goal
        popupBinding.acceptButton.setOnClickListener {
            val goalTitle = popupBinding.goalInput.text.toString().trim()
            if (goalTitle.isEmpty()) {
                Toast.makeText(this, "Goal title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stepTitles = mutableListOf<String>()
            for (i in 0 until popupBinding.stepsContainer.childCount) {
                val stepContainer = popupBinding.stepsContainer.getChildAt(i) as LinearLayout
                val stepEditText = stepContainer.getChildAt(0) as EditText
                val stepTitle = stepEditText.text.toString().trim()
                if (stepTitle.isNotEmpty()) {
                    stepTitles.add(stepTitle)
                }
            }

            val userId = getUserId()
            if (userId != null) {
                Log.d("MainActivity", "Submitting goal with title='$goalTitle' and steps=$stepTitles for userId=$userId")
                mainViewModel.prepareAndSubmitGoal(userId, goalTitle, stepTitles)
            } else {
                Log.w("MainActivity", "UserId is null, cannot submit goal.")
            }

            popupBinding.root.visibility = View.GONE
            resetPopup()
        }
    }

    private fun resetPopup() {
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

        hamburgerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }

    private fun setupNavigationMenu() {
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        findViewById<TextView>(R.id.nav_logout).setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("LifeAssistPrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserId(): String? {
        val userId = getSharedPreferences("LifeAssistPrefs", Context.MODE_PRIVATE)
            .getString("userId", null)
        Log.d("MainActivity", "getUserId returned: $userId")
        return userId
    }
}
