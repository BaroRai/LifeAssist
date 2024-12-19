package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.lifeassist.R
import com.example.lifeassist.databinding.ActivityMainBinding
import com.example.lifeassist.databinding.PopupGoalBinding
import com.example.lifeassist.utils.NavigationDrawerHelper
import com.example.lifeassist.utils.SharedPreferencesHelper
import com.example.lifeassist.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var popupBinding: PopupGoalBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SharedPreferencesHelper.logCurrentState(this) //DEBUG CODE

        NavigationDrawerHelper.setupDrawer(this)
        NavigationDrawerHelper.setupNavigationMenu(this, "Home", binding.drawerLayout)

        setupObservers()
        setupPopup()

        // Fetch all user-related data via ViewModel
        Log.d("MainActivity", "Initializing user data")
        mainViewModel.initializeUserData(this)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.fetchUserData(this)
    }

    private fun setupObservers() {
        mainViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            Log.d("MainActivity", "isLoggedIn changed: $isLoggedIn")
            if (!isLoggedIn) redirectToLogin()
        }

        mainViewModel.mainData.observe(this) { mainData ->
            mainData?.let { data ->
                Log.d("MainActivity", "User data loaded successfully: ${data.user.username}")
                val descriptionText = data.user.description ?: "No description available"
                binding.descriptionTextView.text = descriptionText

                val goalsList = binding.goalsList
                goalsList.removeAllViews()

                if (data.user.goals.isEmpty()) {
                    Log.w("MainActivity", "No goals found to display.")
                    return@let
                }

                data.user.goals.forEach { goal ->
                    val completedStepsCount = goal.steps.count { it.status == "completed" }
                    val totalStepsCount = goal.steps.size

                    val goalCard = CardView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 16, 16, 16)
                        }
                        radius = 24f
                        setContentPadding(16, 16, 16, 16)
                        cardElevation = 4f
                    }

                    val cardContent = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                    }

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
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }
                    goalTitleLayout.addView(goalTextView)

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

                    cardContent.addView(goalTitleLayout)

                    goal.steps.forEach { step ->
                        val stepLayout = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 8, 0, 8)
                            }
                        }

                        val stepTextView = TextView(this).apply {
                            text = step.title
                            textSize = 14f
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }
                        stepLayout.addView(stepTextView)

                        val stepCheckBox = CheckBox(this).apply {
                            isChecked = step.status == "completed"
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        stepCheckBox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                step.status = "completed"
                                val newCompletedCount = goal.steps.count { it.status == "completed" }
                                progressTextView.text = "$newCompletedCount/$totalStepsCount"

                                if (newCompletedCount == totalStepsCount) {
                                    AlertDialog.Builder(this@MainActivity).apply {
                                        setTitle("Complete Goal")
                                        setMessage("You have completed all steps for this goal. Do you want to mark the goal as completed?")
                                        setPositiveButton("Yes") { _, _ ->
                                            goal.status = "completed"
                                            if (goal.id != null) {
                                                mainViewModel.prepareAndUpdateGoalStatus(
                                                    this@MainActivity, goal.id, "completed"
                                                )
                                                Toast.makeText(this@MainActivity, "Goal marked as completed!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@MainActivity, "Error: Cannot update goal.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        setNegativeButton("No") { dialog, _ ->
                                            stepCheckBox.isChecked = false
                                            step.status = "pending"
                                            dialog.dismiss()
                                        }
                                        show()
                                    }
                                }
                            } else {
                                step.status = "pending"
                                val newCompletedCount = goal.steps.count { it.status == "completed" }
                                progressTextView.text = "$newCompletedCount/$totalStepsCount"
                            }
                        }

                        stepLayout.addView(stepCheckBox)
                        cardContent.addView(stepLayout)
                    }
                    goalCard.addView(cardContent)
                    goalsList.addView(goalCard)
                }
            } ?: Log.e("MainActivity", "mainData is null, unable to display user information.")
        }

        mainViewModel.error.observe(this) { error ->
            Log.e("MainActivity", "Error observed: $error")
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupPopup() {
        popupBinding = PopupGoalBinding.inflate(layoutInflater)
        binding.container.addView(popupBinding.root)
        popupBinding.root.visibility = View.GONE

        binding.openPopupButton.setOnClickListener {
            popupBinding.root.visibility = View.VISIBLE
            resetPopup()
        }

        popupBinding.closeButton.setOnClickListener {
            popupBinding.root.visibility = View.GONE
        }

        val stepTitles = mutableSetOf<String>()
        popupBinding.addStepButton.setOnClickListener {
            val stepText = popupBinding.stepInput.text.toString().trim()
            if (stepText.isEmpty()) {
                Toast.makeText(this, "Step cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!stepTitles.add(stepText)) {
                Toast.makeText(this, "Step already exists.", Toast.LENGTH_SHORT).show()
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
                stepTitles.remove(stepText)
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

        popupBinding.acceptButton.setOnClickListener {
            val goalTitle = popupBinding.goalInput.text.toString().trim()
            if (goalTitle.isEmpty()) {
                Toast.makeText(this, "Goal title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stepTitlesList = mutableListOf<String>()
            for (i in 0 until popupBinding.stepsContainer.childCount) {
                val stepContainer = popupBinding.stepsContainer.getChildAt(i) as LinearLayout
                val stepEditText = stepContainer.getChildAt(0) as EditText
                val stepTitle = stepEditText.text.toString().trim()
                if (stepTitle.isNotEmpty()) {
                    stepTitlesList.add(stepTitle)
                }
            }

            mainViewModel.prepareAndSubmitGoal(this, goalTitle, stepTitlesList)
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
}
