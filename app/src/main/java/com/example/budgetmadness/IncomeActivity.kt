package com.example.budgetmadness

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.view.View
import android.view.animation.AnimationUtils
/**
 * Activity that allows users to input and save their income (cash or card).
 * Income data is stored in Firebase, and a confirmation animation is shown upon success.
 * Includes bottom navigation to other key areas of the app.
 */

class IncomeActivity : AppCompatActivity() {

    private lateinit var editCashIncome: EditText
    private lateinit var editCardIncome: EditText
    private lateinit var btnSave: Button
    private lateinit var catAnimationImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)
        // Initialize UI components
        editCashIncome = findViewById(R.id.edit_cash_income)
        editCardIncome = findViewById(R.id.edit_card_income)
        btnSave = findViewById(R.id.btn_save)
        catAnimationImage = findViewById(R.id.incomeCatAnimation)
        // Save income data when button is clicked
        btnSave.setOnClickListener {
            val cashText = editCashIncome.text.toString().trim()
            val cardText = editCardIncome.text.toString().trim()

            val cash = cashText.toDoubleOrNull() ?: 0.0
            val card = cardText.toDoubleOrNull() ?: 0.0
            // Require at least one input
            if (cash == 0.0 && card == 0.0) {
                Toast.makeText(this, "Please enter income", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid/income")
            val incomeEntry = mapOf(
                "cash" to cash,
                "card" to card,
                "timestamp" to System.currentTimeMillis()
            )
            // Save to Firebase
            dbRef.push().setValue(incomeEntry).addOnSuccessListener {
                Toast.makeText(this, "Income saved", Toast.LENGTH_SHORT).show()
                editCashIncome.text.clear()
                editCardIncome.text.clear()
                startCatAnimation()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to save income", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Navigation setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_income -> {
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, StarterPageActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddExpensesActivity::class.java))
                    true
                }
                R.id.nav_open_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    // Runs a short animation when income is successfully saved
    private fun startCatAnimation() {
        val animationView = findViewById<ImageView>(R.id.incomeCatAnimation)

        // Set the frame animation
        animationView.setBackgroundResource(R.drawable.cat_wallet_animation)
        val frameAnimation = animationView.background as AnimationDrawable

        // Apply translate animation
        val move = AnimationUtils.loadAnimation(this, R.anim.move_cat_right)

        animationView.visibility = View.VISIBLE
        animationView.startAnimation(move)
        frameAnimation.start()

        // Cleanup after animation ends
        Handler(Looper.getMainLooper()).postDelayed({
            frameAnimation.stop()
            animationView.clearAnimation()
            animationView.visibility = View.GONE
        }, 2000) // total duration should match translation + buffer
    }
}