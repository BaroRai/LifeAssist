package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
                    // Count completed steps
                    val completedStepsCount = goal.steps.count { it.status == "completed" }
                    val totalStepsCount = goal.steps.size

                    // Create a CardView for each goal
                    val goalCard = CardView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 16, 16, 16) // Add margin around each card
                        }
                        radius = 24f // Rounded corners
                        setContentPadding(16, 16, 16, 16)
                        cardElevation = 4f // Shadow for the card
                    }

                    // Create a vertical layout to hold goal title and steps
                    val cardContent = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                    }

                    // Add goal title with progress indicator
                    val goalTitleLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(0, 0, 0, 16)
                    }

                    val goalTextView = TextView(this).apply {
                        text = "Goal: ${goal.title}"
                        textSize = 18f
                        setTypeface(null, Typeface.BOLD)
                        layoutParams = LinearLayout.LayoutParams(
                            0, // Weight-based width
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f // Take up remaining space
                        )
                    }
                    goalTitleLayout.addView(goalTextView)

                    // Progress indicator (e.g., "2/4")
                    val progressTextView = TextView(this).apply {
                        text = "$completedStepsCount/$totalStepsCount"
                        textSize = 16f
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        gravity = Gravity.END
                    }
                    goalTitleLayout.addView(progressTextView)

                    // Add title layout to card content
                    cardContent.addView(goalTitleLayout)

                    // Add steps with CheckBox
                    goal.steps.forEach { step ->
                        val stepLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 8, 0, 8) // Add spacing between steps
                            }
                        }

                        // Step text
                        val stepTextView = TextView(this).apply {
                            text = step.title
                            textSize = 14f
                            layoutParams = LinearLayout.LayoutParams(
                                0, // Weight-based width
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f // Take up remaining space
                            )
                        }
                        stepLayout.addView(stepTextView)

                        // Step CheckBox
                        val stepCheckBox = CheckBox(this).apply {
                            isChecked = step.status == "completed"
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // Handle CheckBox changes
                        stepCheckBox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                // Mark step as completed
                                step.status = "completed"

                                // Update progress indicator
                                val newCompletedCount = goal.steps.count { it.status == "completed" }
                                progressTextView.text = "$newCompletedCount/$totalStepsCount"

                                // Check if all steps are completed
                                if (newCompletedCount == totalStepsCount) {
                                    // Show confirmation dialog
                                    AlertDialog.Builder(this@MainActivity).apply {
                                        setTitle("Complete Goal")
                                        setMessage("You have completed all steps for this goal. Do you want to mark the goal as completed?")
                                        setPositiveButton("Yes") { _, _ ->
                                            // Mark goal as completed in the database
                                            goal.status = "completed"
                                            // Update the database or ViewModel here
                                            Toast.makeText(this@MainActivity, "Goal marked as completed!", Toast.LENGTH_SHORT).show()
                                        }
                                        setNegativeButton("No") { dialog, _ ->
                                            // Uncheck the last step
                                            stepCheckBox.isChecked = false // Use the local stepCheckBox
                                            step.status = "pending"
                                            dialog.dismiss()
                                        }
                                        show()
                                    }
                                }
                            } else {
                                // Mark step as pending
                                step.status = "pending"
                                val newCompletedCount = goal.steps.count { it.status == "completed" }
                                progressTextView.text = "$newCompletedCount/$totalStepsCount"
                            }
                        }

                        stepLayout.addView(stepCheckBox)
                        // Add step layout to card content
                        cardContent.addView(stepLayout)
                    }
                    // Add content to the card
                    goalCard.addView(cardContent)
                    // Add the card to the goals list
                    goalsList.addView(goalCard)
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
