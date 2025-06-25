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
 * MenuActivity provides a centralized navigation hub for the application.
 * Users can access all major screens from here, including Home, Profile,
 * Expenses, Income, Budget, and more.
 *
 * This directly supports the Menu feature mentioned in the README, allowing
 * users to conveniently move between all core
*/
class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        //Navigates to home screen
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            logEvent("User clicked on Home button")
            startActivity(Intent(this, HomeScreenActivity::class.java))
        }
        //navigates to profile
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            logEvent("User clicked on Profile button")
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        //navigates to expense view screen
        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            logEvent("User clicked on View Expenses button")
            startActivity(Intent(this, ExpenseViewActivity::class.java))
        }
        //navigates to add expense screen
        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            logEvent("User clicked on Add Expense button")
            startActivity(Intent(this, AddExpensesActivity::class.java))
        }
        //navigates to income screen
        findViewById<Button>(R.id.btnIncome).setOnClickListener {
            logEvent("User clicked on Income button")
            startActivity(Intent(this, IncomeActivity::class.java))
        }
        //navigates to balance screen
        findViewById<Button>(R.id.btnBalance).setOnClickListener {
            logEvent("User clicked on Balance button")
            startActivity(Intent(this, BalanceActivity::class.java))
        }
        //navigates to add budget screen
        findViewById<Button>(R.id.btnBudget).setOnClickListener {
            logEvent("User clicked on Budget button")
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        //navigates to add categories screen
        findViewById<Button>(R.id.btnCategories).setOnClickListener {
            logEvent("User clicked on Categories button")
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
        //navigates to view budget screen
        findViewById<Button>(R.id.btnViewBudget).setOnClickListener {
            logEvent("User clicked on View Budget button")
            startActivity(Intent(this, BudgetViewActivity::class.java))
        }
        //navigates to view categories screen
        findViewById<Button>(R.id.btnViewCategories).setOnClickListener {
            logEvent("User clicked on View Categories button")
            startActivity(Intent(this, ViewCategoriesActivity::class.java))
        }
        //navigates to add payment days screen
        findViewById<Button>(R.id.btnBilling).setOnClickListener {
            logEvent("User clicked on Billing button")
            startActivity(Intent(this, PaymentsDueSoonActivity::class.java))
        }
    }
    //logs interactions
    private fun logEvent(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp - $message"
        android.util.Log.d("MenuActivityLog", logMessage)

        try {
            val file = File(getExternalFilesDir(null), "app_log.txt")
            val writer = FileWriter(file, true)
            writer.appendLine(logMessage)
            writer.close()
        } catch (e: IOException) {
            android.util.Log.e("MenuActivityLog", "Failed to write log: ${e.message}")
        }
    }
}
