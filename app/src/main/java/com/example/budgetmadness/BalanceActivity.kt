package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetmadness.models.IncomeEntry
import com.example.budgetmadness.models.Expense
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
/** BalanceActivity displays the users current balance by calculating the difference between total income
 * and the latest expense.
 * It also shows the full income and expense history using RecyclerViews.
 */
class BalanceActivity : AppCompatActivity() {

    private val TAG = "BalanceActivity"
    //UI components
    private lateinit var totalIncomeText: TextView
    private lateinit var recyclerViewIncome: RecyclerView
    private lateinit var recyclerViewExpenses: RecyclerView
    private lateinit var incomeAdapter: IncomeHistoryAdapter
    private lateinit var expenseAdapter: ExpenseHistoryAdapter

    //lists to hold income and expenses entered
    private val incomeList = mutableListOf<IncomeEntry>()
    private val expenseList = mutableListOf<Expense>()

    //handler for updating the balance periodicly
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 2000L
    private var latestExpense = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)
        Log.d(TAG, "onCreate called")

        totalIncomeText = findViewById(R.id.text_total_income)
        recyclerViewIncome = findViewById(R.id.recycler_income_history)
        recyclerViewExpenses = findViewById(R.id.recycler_expense_history)
        //set up income recycler view
        incomeAdapter = IncomeHistoryAdapter(incomeList)
        recyclerViewIncome.layoutManager = LinearLayoutManager(this)
        recyclerViewIncome.adapter = incomeAdapter
        //set up expense recycler view
        expenseAdapter = ExpenseHistoryAdapter(expenseList)
        recyclerViewExpenses.layoutManager = LinearLayoutManager(this)
        recyclerViewExpenses.adapter = expenseAdapter
        //fetch both income and expenses from firebase
        fetchIncomeFromFirebase()
        fetchExpensesFromFirebase()

        //navigation between screens
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

    /** Fetches income enteries from Firebase and updates the income recycler view
     * also calculates total income from both cash and card sources
     */
    private fun fetchIncomeFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/income")
        ref.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                incomeList.clear()
                for (child in snapshot.children) {
                    val entry = child.getValue(IncomeEntry::class.java)
                    if (entry != null) incomeList.add(entry)
                }
                incomeList.reverse()
                incomeAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load income: ${error.message}")
            }
        })
    }

    /** Fetches expense entries from Firebase and updates the expense RecyclerView.
     * Also retrieves the most recent expense for balance calculations
     */
    private fun fetchExpensesFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/expenses")
        ref.orderByChild("date").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseList.clear()
                latestExpense = 0.0

                for (child in snapshot.children) {
                    val entry = child.getValue(Expense::class.java)
                    if (entry != null) expenseList.add(entry)
                }

                expenseList.sortByDescending { it.date }
                if (expenseList.isNotEmpty()) {
                    latestExpense = expenseList.first().amount
                }

                expenseAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load expenses: ${error.message}")
            }
        })
    }
    //starts balance update when activity is visible
    override fun onResume() {
        super.onResume()
        handler.post(updateBalanceRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateBalanceRunnable)
    }
    //updates balance based on the income received
    private val updateBalanceRunnable = object : Runnable {
        override fun run() {
            val totalIncome = incomeList.sumOf { it.cash + it.card }
            val balance = totalIncome - latestExpense
            //displays the calculated balance
            totalIncomeText.text = "Total Balance: R%.2f".format(balance)
            handler.postDelayed(this, updateInterval)
        }
    }
}