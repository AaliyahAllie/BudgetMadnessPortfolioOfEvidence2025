package com.example.budgetmadness

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmadness.models.Budget
import com.example.budgetmadness.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * BudgetViewActivity allows users to:
 * - Select a date range and a month.
 * - View how much they’ve spent in that period.
 * - Compare that amount to their stored budget.
 * - Receive visual feedback with motivational cat images.
 */

class BudgetViewActivity : AppCompatActivity() {
    //UI Elements
    private lateinit var viewMonthSpinner: Spinner
    private lateinit var editStartDate: EditText
    private lateinit var editEndDate: EditText
    private lateinit var viewButton: Button
    private lateinit var budgetDetailsText: TextView
    private lateinit var rewardImage: ImageView
    // Date format used for parsing and displaying
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Predefined list of months used in Spinner
    private val months = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_view)
        // Bind UI components to layout views
        viewMonthSpinner = findViewById(R.id.viewMonthSpinner)
        editStartDate = findViewById(R.id.editStartDate)
        editEndDate = findViewById(R.id.editEndDate)
        viewButton = findViewById(R.id.viewButton)
        budgetDetailsText = findViewById(R.id.budgetDetailsText)
        rewardImage = findViewById(R.id.rewardImage)
        rewardImage.visibility = View.GONE
        // Populate the month dropdown
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewMonthSpinner.adapter = adapter
        // Set date pickers for the start and end date inputs
        editStartDate.setOnClickListener { showDatePicker(editStartDate) }
        editEndDate.setOnClickListener { showDatePicker(editEndDate) }
        //On click, validate input and trigger budget evaluation
        viewButton.setOnClickListener {
            val selectedMonth = viewMonthSpinner.selectedItem.toString()
            val startDate = editStartDate.text.toString()
            val endDate = editEndDate.text.toString()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            } else {
                fetchExpensesAndCompareToBudget(selectedMonth, startDate, endDate)
            }
        }
    }
    /**
     * Launches a DatePickerDialog and sets the selected date into the target EditText.
     */
    private fun showDatePicker(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, day)
            target.setText(date)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
    /**
     * Fetches all user expenses and filters those that fall within the selected date range.
     * Then calculates the total spent and compares it to the budget.
     */
    private fun fetchExpensesAndCompareToBudget(month: String, startDate: String, endDate: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/expenses")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses = mutableListOf<Expense>()
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java)
                    if (expense != null && isWithinRange(expense.date, startDate, endDate)) {
                        expenses.add(expense)
                    }
                }
                // Calculate total spent in the date range
                val totalSpent = expenses.sumOf { it.amount }
                fetchBudgetTargets(month, totalSpent)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BudgetViewActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
        })
    }
    /**
     * Checks if a given date string falls within the provided start and end range.
     */
    private fun isWithinRange(dateStr: String, start: String, end: String): Boolean {
        return try {
            val date = dateFormat.parse(dateStr)
            val startDate = dateFormat.parse(start)
            val endDate = dateFormat.parse(end)
            date != null && startDate != null && endDate != null &&
                    !date.before(startDate) && !date.after(endDate)
        } catch (e: Exception) {
            false
        }
    }
    /**
     * Fetches the user’s budget for the selected month, then compares it to total spent.
     * Displays results with messages and reward images depending on how the user performed.
     */

    private fun fetchBudgetTargets(month: String, totalSpent: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/budgets/$month")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val budget = snapshot.getValue(Budget::class.java)

                if (budget == null) {
                    budgetDetailsText.text = "No budget data found for $month."
                    rewardImage.visibility = View.GONE
                    return
                }

                val min = budget.min
                val max = budget.max

                val (message, imageRes) = when {
                    totalSpent in min..max -> Pair(
                        "🎯 Great job! You stuck to your budget.",
                        R.drawable.chill_cat
                    )
                    totalSpent < min -> Pair(
                        "😺 You're under budget. Very responsible!",
                        R.drawable.happy_cat
                    )
                    else -> Pair(
                        "🙀 Over budget. Time for the disappointed cat stare.",
                        R.drawable.disappointed_cat
                    )
                }
                // Show feedback on screen
                rewardImage.setImageResource(imageRes)
                rewardImage.visibility = View.VISIBLE

                budgetDetailsText.text = """
                    Month: $month
                    Total Spent: R${"%.2f".format(totalSpent)}
                    Budget Range: R${"%.2f".format(min)} - R${"%.2f".format(max)}
                    
                    $message
                """.trimIndent()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BudgetViewActivity, "Failed to load budget", Toast.LENGTH_SHORT).show()
            }
        })
    }
}