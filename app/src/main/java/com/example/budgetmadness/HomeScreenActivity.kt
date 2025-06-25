package com.example.budgetmadness

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmadness.models.Budget
import com.example.budgetmadness.models.Expense
import com.example.budgetmadness.models.IncomeEntry
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
/**
 * Activity that displays the user's financial overview including latest income,
 * latest expenses, total balance, and a visual pie chart of budget usage.
 * Allows selecting a date range to generate budget analysis, in the form of a pie chart
 */

class HomeScreenActivity : AppCompatActivity() {
    // UI Components
    private lateinit var incomeTextView: TextView
    private lateinit var expenseTextView: TextView
    private lateinit var balanceTextView: TextView
    private lateinit var pieChart: PieChart
    private lateinit var editStartDate: EditText
    private lateinit var editEndDate: EditText
    private lateinit var generateChartButton: Button
    private lateinit var budgetDetailsText: TextView
    // Timer for updating UI
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L
    // Date formatter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Financial data tracking
    private var latestIncome = 0.0
    private var totalIncome = 0.0
    private var latestExpense = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        // Link UI elements
        incomeTextView = findViewById(R.id.incomeTextView)
        expenseTextView = findViewById(R.id.textViewExpenses)
        balanceTextView = findViewById(R.id.textViewBalance)
        pieChart = findViewById(R.id.homePieChart)
        editStartDate = findViewById(R.id.editStartDate)
        editEndDate = findViewById(R.id.editEndDate)
        generateChartButton = findViewById(R.id.generateChartButton)
        budgetDetailsText = findViewById(R.id.homeBudgetDetailsText)
        // Date picker setup
        editStartDate.setOnClickListener { showDatePicker(editStartDate) }
        editEndDate.setOnClickListener { showDatePicker(editEndDate) }
        // Generate pie chart based on selected range
        generateChartButton.setOnClickListener {
            val start = editStartDate.text.toString()
            val end = editEndDate.text.toString()
            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show()
            } else {
                loadPieChartForRange(start, end)
            }
        }
        // Fetch latest income and expenses
        fetchIncomeFromFirebase()
        fetchExpensesFromFirebase()
        handler.post(updateUIRunnable)
        //Navigation of screens
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener { item ->
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
    // Displays a date picker dialog and updates selected
    private fun showDatePicker(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            target.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
    // Retrieves total income and most recent income value from Firebase
    private fun fetchIncomeFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/income")
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val incomeList = snapshot.children.mapNotNull { it.getValue(IncomeEntry::class.java) }
                    totalIncome = incomeList.sumOf { it.cash + it.card }
                    latestIncome = incomeList.maxByOrNull { it.timestamp }?.let { it.cash + it.card } ?: 0.0
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    // Retrieves most recent expense amount from Firebase
    private fun fetchExpensesFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/expenses")
            .orderByChild("date")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenseList = snapshot.children.mapNotNull { it.getValue(Expense::class.java) }
                    latestExpense = expenseList.maxByOrNull { it.date }?.amount ?: 0.0
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    // Runnable that updates income, expense, and balance on the UI every 5 seconds
    private val updateUIRunnable = object : Runnable {
        override fun run() {
            val balance = totalIncome - latestExpense
            incomeTextView.text = "+R%.2f".format(latestIncome)
            expenseTextView.text = "-R%.2f".format(latestExpense)
            balanceTextView.text = "R%.2f".format(balance)
            handler.postDelayed(this, updateInterval)
        }
    }
    // Generates pie chart and budget analysis message for a selected date range
    private fun loadPieChartForRange(startDateStr: String, endDateStr: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        // Step 1: Get expenses in date range
        val startDate = dateFormat.parse(startDateStr)
        val endDate = dateFormat.parse(endDateStr)

        if (startDate == null || endDate == null || startDate.after(endDate)) {
            Toast.makeText(this, "Invalid date range", Toast.LENGTH_SHORT).show()
            return
        }

        ref.child("expenses").get().addOnSuccessListener { expenseSnap ->
            val expenses = expenseSnap.children.mapNotNull { it.getValue(Expense::class.java) }
                .filter {
                    try {
                        val date = dateFormat.parse(it.date)
                        date != null && !date.before(startDate) && !date.after(endDate)
                    } catch (_: Exception) {
                        false
                    }
                }

            val totalSpent = expenses.sumOf { it.amount }
            // Step 2: Determine the relevant budget using month name as key
            val calendar = Calendar.getInstance().apply { time = startDate }
            val budgetKey = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "January"
            // Step 3: Get the budget and calculate remaining/over
            ref.child("budgets/$budgetKey").get().addOnSuccessListener { budgetSnap ->
                val budget = budgetSnap.getValue(Budget::class.java)
                if (budget == null) {
                    Toast.makeText(this, "No budget for $budgetKey", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val min = budget.min
                val max = budget.max
                val remaining = max - totalSpent
                val over = if (remaining < 0) -remaining else 0.0
                // Show budget summary message
                val message = when {
                    totalSpent < min -> "😺 You're under budget. Responsible!"
                    totalSpent <= max -> "🎯 Great job staying within budget!"
                    else -> "🙀 Overspent by R${"%.2f".format(over)}"
                }

                budgetDetailsText.text = """
                    📅 Range: $startDateStr – $endDateStr
                    💸 Total Spent: R${"%.2f".format(totalSpent)}
                    📊 Budget: R${"%.2f".format(min)} – R${"%.2f".format(max)}
                    ${if (remaining >= 0) "🟢 Remaining: R${"%.2f".format(remaining)}" else "🔴 Over Budget: R${"%.2f".format(over)}"}
                    
                    $message
                """.trimIndent()
                budgetDetailsText.visibility = View.VISIBLE
                // Step 4: Build pie chart
                val entries = mutableListOf<PieEntry>().apply {
                    add(PieEntry(totalSpent.toFloat(), "Spent"))
                    if (remaining > 0) {
                        add(PieEntry(remaining.toFloat(), "Remaining"))
                    } else {
                        add(PieEntry(over.toFloat(), "Over"))
                    }
                }

                val colors = listOf(
                    Color.parseColor("#4FC3F7"),
                    Color.parseColor("#AED581"),
                    Color.parseColor("#E57373")
                )

                val dataSet = PieDataSet(entries, "").apply {
                    this.colors = colors
                    valueTextSize = 12f
                    sliceSpace = 4f
                }

                pieChart.apply {
                    data = PieData(dataSet)
                    setUsePercentValues(false)
                    holeRadius = 60f
                    transparentCircleRadius = 65f
                    setCenterTextSize(14f)
                    setCenterTextColor(Color.DKGRAY)
                    centerText = "R${"%.2f".format(totalSpent)} Spent"
                    setEntryLabelColor(Color.BLACK)
                    setDrawEntryLabels(true)
                    description.text = ""
                    legend.isEnabled = true
                    animateY(1000)
                    invalidate()
                }
            }
        }
    }
    // Stop auto-updating UI
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateUIRunnable)
    }
}
