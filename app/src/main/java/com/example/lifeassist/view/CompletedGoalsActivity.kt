package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.lifeassist.R
import com.example.lifeassist.utils.NavigationDrawerHelper
import com.example.lifeassist.viewmodel.CompletedGoalsViewModel
import com.example.lifeassist.databinding.ActivityCompletedGoalsBinding
import com.example.lifeassist.databinding.ActivityLoginBinding
import com.example.lifeassist.utils.SharedPreferencesHelper

class CompletedGoalsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompletedGoalsBinding
    private val completedGoalsViewModel: CompletedGoalsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompletedGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val completedGoalsList = binding.completedGoalsList // Use binding to access views.

        // Setup Drawer
        NavigationDrawerHelper.setupDrawer(this)
        NavigationDrawerHelper.setupNavigationMenu(this, "completedGoals", binding.drawerLayout)

        setupObservers(completedGoalsList)
        fetchCompletedGoals(this)
    }

    private fun setupObservers(completedGoalsList: LinearLayout) {
        // Observe Completed Goals
        completedGoalsViewModel.completedGoals.observe(this) { completedGoals ->
            completedGoalsList.removeAllViews() // Clear old views

            completedGoals.forEach { goal ->
                // Create a CardView for each completed goal
                val goalCard = CardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 16, 16, 16) // Add margin around each card
                    }
                    radius = 24f
                    setContentPadding(16, 16, 16, 16)
                    cardElevation = 4f
                }

                val cardContent = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val goalTextView = TextView(this).apply {
                    text = "Goal: ${goal.title}"
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                }
                cardContent.addView(goalTextView)

                goal.steps.forEach { step ->
                    val stepTextView = TextView(this).apply {
                        text = "- ${step.title} (${step.status})"
                        textSize = 14f
                        setPadding(8, 8, 8, 8)
                    }
                    cardContent.addView(stepTextView)
                }

                goalCard.addView(cardContent)
                completedGoalsList.addView(goalCard)
            }

            if (completedGoals.isEmpty()) {
                val emptyMessage = TextView(this).apply {
                    text = "No completed goals yet!"
                    textSize = 16f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                completedGoalsList.addView(emptyMessage)
            }
        }

        // Observe Errors
        completedGoalsViewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchCompletedGoals(context: Context) {
        val userId = SharedPreferencesHelper.getUserId(this)
        Log.d("CompletedGoalsActivity", "Retrieved userId from SharedPreferences: $userId")

        if (userId.isNullOrEmpty()) {
            Log.d("CompletedGoalsActivity", "No userId found, redirecting to login.")
            redirectToLogin()
        } else {
            Log.d("CompletedGoalsActivity", "Calling fetchCompletedGoals with userId: $userId")
            completedGoalsViewModel.fetchCompletedGoals(context, userId)
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

