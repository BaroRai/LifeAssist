package com.example.lifeassist.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lifeassist.R
import com.example.lifeassist.databinding.ActivityMainBinding
import androidx.activity.viewModels
import com.example.lifeassist.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val mainViewModel: MainViewModel by viewModels() // ViewModel instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user is logged in
        mainViewModel.checkIfUserIsLoggedIn(this)

        // Observe the logged-in status from ViewModel
        mainViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (!isLoggedIn) {
                redirectToLogin() // Redirect to Login if not logged in
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
                binding.usernameTextView.text = it.username  // Display username
                binding.descriptionTextView.text = it.description  // Display description
            }
        }

        // Observe errors from ViewModel
        mainViewModel.error.observe(this) { errorMessage ->
            // Show error message if any
            Log.e("MainActivity", errorMessage)
        }

        // Handle Logout Button Click
        binding.navLogout.setOnClickListener {
            Log.d("Logout", "I was clicked! (logout)")
            mainViewModel.logoutUser(this) // Call logout function from ViewModel
            redirectToLogin() // Redirect after logout
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

        // Handle navigation menu items (Home, Profile, Settings)
        binding.navHome.setOnClickListener {
            // Handle home navigation logic
        }
        binding.navProfile.setOnClickListener {
            // Handle profile navigation logic
        }
        binding.navSettings.setOnClickListener {
            // Handle settings navigation logic
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Close MainActivity and open LoginActivity
    }
}
