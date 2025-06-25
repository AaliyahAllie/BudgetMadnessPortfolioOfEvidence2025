package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
/**
 * MainActivity serves as the launcher screen for the application.
 * It presents the user with options to either register or log in.
 * This aligns with Feature 1 from the README: App Launcher
 * */
class MainActivity : AppCompatActivity() {
    //UI COMPONENTS
    private lateinit var registerBtn: Button
    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //LINKS UI AND LAYOUT VIEWS
        registerBtn = findViewById(R.id.registerBtn)
        loginBtn = findViewById(R.id.loginBtn)

        //TAKES USER TO REGISTRATION SCREEN ON CLICK
        registerBtn.setOnClickListener {
            logEvent("User clicked on Register button")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        //TAKES USER TO LOGIN ON CLICK
        loginBtn.setOnClickListener {
            logEvent("User clicked on Login button")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    //LOGS INFORMATION HAPPENING ON THE APPLICATION WHILE RUNNING
    private fun logEvent(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp - $message"
        android.util.Log.d("MainActivityLog", logMessage)

        try {
            val file = File(getExternalFilesDir(null), "app_log.txt")
            val writer = FileWriter(file, true)
            writer.appendLine(logMessage)
            writer.close()
        } catch (e: IOException) {
            android.util.Log.e("MainActivityLog", "Failed to write log: ${e.message}")
        }
    }
}
