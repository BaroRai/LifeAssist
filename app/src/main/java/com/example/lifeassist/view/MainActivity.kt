package com.example.lifeassist.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.lifeassist.R
import com.example.lifeassist.databinding.ActivityMainBinding
import com.example.lifeassist.databinding.PopupGoalBinding
import com.example.lifeassist.model.Main
import com.example.lifeassist.utils.NavigationDrawerHelper
import com.example.lifeassist.utils.SharedPreferencesHelper
import com.example.lifeassist.viewmodel.MainViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var popupBinding: PopupGoalBinding
    private val mainViewModel: MainViewModel by viewModels()

    // Keep a local copy of all goals to support filtering.
    private var allGoals = listOf<Main.Goal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SharedPreferencesHelper.logCurrentState(this) // DEBUG

        NavigationDrawerHelper.setupDrawer(this)
        NavigationDrawerHelper.setupNavigationMenu(this, "Home", binding.drawerLayout)

        setupObservers()
        setupPopup()
        setupGoalFilter()

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
                // Update welcome text with current user's name.
                val welcomeTextView = binding.mainScreenText.findViewById<TextView>(R.id.mainScreenText)
                welcomeTextView.text = "Welcome, ${data.user.username}"

                // Update description TextView.
                val descriptionText = data.user.description ?: "No description available"
                binding.descriptionTextView.text = descriptionText

                // Save goals locally and display them.
                allGoals = data.user.goals
                filterAndDisplayGoals()
            } ?: Log.e("MainActivity", "mainData is null, unable to display user information.")
        }

        mainViewModel.error.observe(this) { error ->
            Log.e("MainActivity", "Error observed: $error")
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    // --- Goal Filtering and Display ---

    // Filter the stored goals according to the filter text and spinner selection.
    private fun filterAndDisplayGoals() {
        // Get filter text and spinner selection.
        val filterText = binding.goalFilterEditText.text.toString().trim().lowercase(Locale.getDefault())
        val sortOption = binding.filterSpinner.selectedItem.toString()

        var filteredGoals = allGoals

        // If filtering by name (if spinner is set to "Name") then filter by substring.
        if (sortOption.equals("Name", ignoreCase = true)) {
            if (filterText.isNotEmpty()) {
                filteredGoals = filteredGoals.filter { it.title.lowercase(Locale.getDefault()).contains(filterText) }
            }
        } else {
            // For Ascending or Descending, first (optionally) filter by substring if provided.
            if (filterText.isNotEmpty()) {
                filteredGoals = filteredGoals.filter { it.title.lowercase(Locale.getDefault()).contains(filterText) }
            }
            filteredGoals = if (sortOption.equals("Ascending", ignoreCase = true)) {
                filteredGoals.sortedBy { it.title }
            } else { // Descending
                filteredGoals.sortedByDescending { it.title }
            }
        }

        displayGoals(filteredGoals)
    }

    // Display the provided list of goals.
    private fun displayGoals(goals: List<Main.Goal>) {
        val goalsList = binding.goalsList
        goalsList.removeAllViews()

        if (goals.isEmpty()) {
            Log.w("MainActivity", "No goals found to display.")
            return
        }

        goals.forEach { goal ->
            val completedStepsCount = goal.steps.count { it.status == "completed" }
            val totalStepsCount = goal.steps.size

            val goalCard = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(16, 16, 16, 16) }
                radius = 24f
                setContentPadding(16, 16, 16, 16)
                cardElevation = 4f
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            // Layout for goal title and timestamp.
            val goalTitleLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Goal title.
            val goalTitleTextView = TextView(this).apply {
                text = "Goal: ${goal.title}"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
            }
            goalTitleLayout.addView(goalTitleTextView)

            // Timestamp (formatted).
            val timestampTextView = TextView(this).apply {
                text = "Created: ${formatTimestamp(goal.createdAt)}"
                textSize = 12f
                setTextColor(resources.getColor(android.R.color.darker_gray))
            }
            goalTitleLayout.addView(timestampTextView)

            cardContent.addView(goalTitleLayout)

            // Progress indicator.
            val progressTextView = TextView(this).apply {
                text = "$completedStepsCount/$totalStepsCount"
                textSize = 16f
                gravity = Gravity.END
            }
            cardContent.addView(progressTextView)

            // Steps list.
            goal.steps.forEach { step ->
                val stepLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 8) }
                }
                val stepTextView = TextView(this).apply {
                    text = step.title
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                stepLayout.addView(stepTextView)
                val stepCheckBox = CheckBox(this).apply {
                    isChecked = step.status == "completed"
                }
                stepCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        step.status = "completed"
                        val newCompletedCount = goal.steps.count { it.status == "completed" }
                        progressTextView.text = "$newCompletedCount/$totalStepsCount"
                        if (newCompletedCount == totalStepsCount) {
                            AlertDialog.Builder(this@MainActivity).apply {
                                setTitle("Complete Goal")
                                setMessage("You have completed all steps for this goal. Mark as completed?")
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
    }

    // Helper: Format timestamp string to "dd/MM/yyyy HH:mm"
    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return "N/A"
        try {
            // Assume input is in ISO 8601 format, e.g., "2024-12-14T12:45:59.749Z"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timestamp // fallback
    }

    // Set up the filter: both an EditText and a Spinner for 3 options.
    private fun setupGoalFilter() {
        // Assume the spinner is in the layout with id filterSpinner.
        val filterEditText = findViewById<EditText>(R.id.goalFilterEditText)
        val filterSpinner = findViewById<Spinner>(R.id.filterSpinner)
        // Set up spinner adapter.
        val options = listOf("Name", "Ascending", "Descending")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        // When either the text changes or the spinner selection changes, update display.
        filterEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterAndDisplayGoals()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterAndDisplayGoals()
            }
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
