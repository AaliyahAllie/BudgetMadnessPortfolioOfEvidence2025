package com.example.budgetmadness

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmadness.models.Expense
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
/**
 * Activity that allows users to view their expenses within a selected date range.
 * Retrieves data from Firebase, displays it in a receipt format, and shows a warning
 * if monthly spending exceeds a set threshold.
 */

class ExpenseViewActivity : AppCompatActivity() {
    //UI COMPONENTS
    private lateinit var textReceiptView: TextView
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val expenseList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_view)
        //INITIALIZES UI COMPONENTS
        val editStartDate = findViewById<EditText>(R.id.editStartDate)
        val editEndDate = findViewById<EditText>(R.id.editEndDate)
        val btnLoad = findViewById<Button>(R.id.btnLoadExpenses)
        textReceiptView = findViewById(R.id.textReceiptView)

        //PROMPTS USER TO SELECT A START DATE AND END DATE
        //USES DATE PICKER DIALOG
        editStartDate.setOnClickListener { showDatePicker(editStartDate) }
        editEndDate.setOnClickListener { showDatePicker(editEndDate) }

        //LOADS INPUT DATES AND CALUCATES THE EXPENSES STORED IN FIREBASE
        btnLoad.setOnClickListener {
            val startDate = editStartDate.text.toString()
            val endDate = editEndDate.text.toString()

            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                fetchExpensesFromFirebase(startDate, endDate)
            } else {
                //IF FIELD IS BLANK USER GETS A TOAST MESSAGE
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            }
        }

        setupBottomNav()
    }
    // Fetch expenses from Firebase for the current user, filtered by date range

    private fun fetchExpensesFromFirebase(startDate: String, endDate: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/expenses")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseList.clear()
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java)
                    if (expense != null && isWithinRange(expense.date, startDate, endDate)) {
                        expenseList.add(expense)
                    }
                }

                val receipt = if (expenseList.isNotEmpty()) {
                    buildReceipt(expenseList)
                } else {
                    "No expenses found in this date range."
                }

                textReceiptView.text = receipt

                // Monthly total check
                val currentMonth = monthFormat.format(Date())
                val monthlyTotal = getMonthlyTotal(expenseList, currentMonth)
                if (monthlyTotal > 6000.0) {
                    val builder = android.app.AlertDialog.Builder(this@ExpenseViewActivity)
                    builder.setTitle("Monthly Limit Exceeded")
                    builder.setMessage("You’ve spent R${"%.2f".format(monthlyTotal)} in $currentMonth — over your R6000 limit!")
                    builder.setPositiveButton("OK", null)
                    builder.show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ExpenseViewActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Helper to check if a date falls within the selected start and end range
    private fun isWithinRange(dateStr: String, start: String, end: String): Boolean {
        return try {
            val date = dateFormat.parse(dateStr)
            val startDate = dateFormat.parse(start)
            val endDate = dateFormat.parse(end)
            date != null && startDate != null && endDate != null && !date.before(startDate) && !date.after(endDate)
        } catch (e: Exception) {
            false
        }
    }
    // Sum all expenses for the given month (used for monthly limit check)
    private fun getMonthlyTotal(expenses: List<Expense>, targetMonth: String): Double {
        return expenses
            .filter { it.date.startsWith(targetMonth) }
            .sumOf { it.amount }
    }
    // Build a formatted receipt string with all expense details
    private fun buildReceipt(expenses: List<Expense>): String {
        val builder = StringBuilder()
        builder.append("========== EXPENSE RECEIPT ==========\n")
        builder.append("From ${expenses.minByOrNull { it.date }?.date} to ${expenses.maxByOrNull { it.date }?.date}\n")
        builder.append("=====================================\n\n")

        var total = 0.0
        for (e in expenses) {
            builder.append("Name   : ${e.name}\n")
            builder.append("Amount : R${"%.2f".format(e.amount)}\n")
            builder.append("Date   : ${e.date}\n")
            builder.append("-------------------------------------\n")
            total += e.amount
        }

        builder.append("\nTOTAL SPENT: R${"%.2f".format(total)}\n")
        builder.append("=====================================\n")
        return builder.toString()
    }
    //METHOD TO INITIALIZE DATE PICKER
    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            targetEditText.setText(dateFormat.format(calendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
    //NAVIGATION OF SCREENS
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_income -> startActivity(Intent(this, IncomeActivity::class.java))
                R.id.nav_home -> startActivity(Intent(this, StarterPageActivity::class.java))
                R.id.nav_add -> startActivity(Intent(this, AddExpensesActivity::class.java))
                R.id.nav_open_menu -> startActivity(Intent(this, MenuActivity::class.java))
                else -> false
            }
            true
        }
    }
}