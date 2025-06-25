package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**BudgetActivity allows users to set montly min and max budgt limits.
 * These values are saved online.
 * Features: Graphs tied to this budget will be shown in the Home Screen as per ReadMe file.
 */
class BudgetActivity : AppCompatActivity() {
    //UI elements
    private lateinit var monthSpinner: Spinner
    private lateinit var minBudgetInput: EditText
    private lateinit var maxBudgetInput: EditText
    private lateinit var updateButton: Button
    private lateinit var budgetHandler: BudgetHandler
    //Handles local and Firebase budget saving logic
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        //UI elements CONNECTED to layout view
        monthSpinner = findViewById(R.id.monthSpinner)
        minBudgetInput = findViewById(R.id.minBudgetInput)
        maxBudgetInput = findViewById(R.id.maxBudgetInput)
        updateButton = findViewById(R.id.updateButton)
        //populate monthly dropdown with array resource
        val months = resources.getStringArray(R.array.months_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter
        //Initialize handler to deal with database interactions
        budgetHandler = BudgetHandler(this)
        //save budget when budget is clicked
        updateButton.setOnClickListener {
            val month = monthSpinner.selectedItem.toString()
            val min = minBudgetInput.text.toString().toDoubleOrNull()
            val max = maxBudgetInput.text.toString().toDoubleOrNull()
        //Validates inputs
            if (min == null || max == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //uploads data to firebase
            try {
                val inserted = budgetHandler.insertBudget(month, min, max)
                budgetHandler.uploadBudgetToFirebase(month, min, max)

                if (inserted) {
                    Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                    minBudgetInput.text.clear()
                    maxBudgetInput.text.clear()
                    val savedBudget = budgetHandler.getBudgetForMonth(month)
                    Log.d("BudgetCheck", "Saved: $savedBudget")
                } else {
                    Toast.makeText(this, "Error saving budget locally", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("BudgetActivity", "Exception saving budget", e)
            }
        }
        //bottom navigation to navigate screens
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_income -> {
                    startActivity(Intent(this, IncomeActivity::class.java))
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
}