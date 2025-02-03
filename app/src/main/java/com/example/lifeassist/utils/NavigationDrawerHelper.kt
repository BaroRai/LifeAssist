package com.example.lifeassist.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lifeassist.R
import com.example.lifeassist.view.CompletedGoalsActivity
import com.example.lifeassist.view.LoginActivity
import com.example.lifeassist.view.MainActivity
import com.example.lifeassist.view.ProfileActivity

object NavigationDrawerHelper {

    fun setupNavigationMenu(
        activity: AppCompatActivity,
        currentView: String,
        drawerLayout: DrawerLayout
    ) {
        val navHome = drawerLayout.findViewById<TextView>(R.id.nav_home)
        navHome.setOnClickListener {
            if (currentView != "Home") {
                val intent = Intent(activity, MainActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            } else {
                Toast.makeText(activity, "You're already on Home", Toast.LENGTH_SHORT).show()
            }
            Log.d("DrawerClick", "Clicked: home")
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        val navProfile = drawerLayout.findViewById<TextView>(R.id.nav_profile)
        navProfile.setOnClickListener {
            val context = activity as AppCompatActivity
            val intent = Intent(activity, ProfileActivity::class.java)
            context.startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        val navCompletedGoals = drawerLayout.findViewById<TextView>(R.id.nav_completed_goals)
        navCompletedGoals.setOnClickListener {
            if (currentView != "completedGoals") {
                val intent = Intent(activity, CompletedGoalsActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            } else {
                Toast.makeText(activity, "You're already on Completed Goals", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        val navLogout = drawerLayout.findViewById<TextView>(R.id.nav_logout)
        navLogout.setOnClickListener {
            activity.getSharedPreferences("LifeAssistPrefs", Context.MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(activity, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    internal fun setupDrawer(activity: AppCompatActivity) {
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
        val hamburgerButton = activity.findViewById<ImageButton>(R.id.hamburger_menu_button)

        hamburgerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
    }
}
