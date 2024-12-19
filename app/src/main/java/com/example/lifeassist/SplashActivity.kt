package com.example.lifeassist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.lifeassist.utils.SharedPreferencesHelper
import com.example.lifeassist.view.MainActivity
import com.example.lifeassist.view.LoginActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay to simulate splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            val userId = SharedPreferencesHelper.getUserId(this)
            if (userId.isNullOrEmpty()) {
                // Navigate to LoginActivity if no userId found
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                // Navigate to MainActivity if userId exists
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish() // Finish SplashActivity
        }, 3000) // 3-second delay
    }
}
