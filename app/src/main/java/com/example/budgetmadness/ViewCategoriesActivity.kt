package com.example.budgetmadness

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmadness.models.Expense
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
/**
 * ViewCategoriesActivity displays total spending grouped by category for a selected date range.
 *
 * It uses a ListView to show each category with total spent, and a PieChart for visual analysis.
 * Implements Feature 14: "View Categories" from the project README.
 */

class ViewCategoriesActivity : AppCompatActivity() {
    //UI ELEMENTS
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var filterButton: Button
    private lateinit var categoryListView: ListView
    private lateinit var pieChart: PieChart

    //DATE RANGE
    private var startDate: Long = 0L
    private var endDate: Long = Long.MAX_VALUE
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_categories)

        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        filterButton = findViewById(R.id.filterButton)
        categoryListView = findViewById(R.id.categoryListView)
        pieChart = findViewById(R.id.categoryPieChart)

        startDateInput.setOnClickListener { showDatePicker(true) }
        endDateInput.setOnClickListener { showDatePicker(false) }
        //FILTERS DATA WHEN BUTTON IS CLICKED
        filterButton.setOnClickListener { fetchAndGroupByCategory() }
    }

    //DATE PICKER DIALOG
    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val dateSet = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day, 0, 0)
            val millis = calendar.timeInMillis
            val dateText = formatter.format(Date(millis))
            //START DATE FOR GENERATION
            if (isStart) {
                startDate = millis
                startDateInput.setText(dateText)
                //END DATE FOR GENERATION
            } else {
                endDate = millis
                endDateInput.setText(dateText)
            }
        }
        DatePickerDialog(
            this, dateSet,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    //GROUPS CATEGORIES AND DISPLAYS IT AS A PIE CHART
    private fun fetchAndGroupByCategory() {
        //FETCHES DATA FROM USER UNDER FIREBASE
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/expenses")

        //GETS DATA
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryTotals = mutableMapOf<String, Double>()
                for (child in snapshot.children) {
                    val entry = child.getValue(Expense::class.java)
                    val dateString = entry?.date
                    val entryTime = try {
                        if (!dateString.isNullOrBlank()) formatter.parse(dateString)?.time else null
                    } catch (e: ParseException) {
                        null
                    }

                    if (entry != null && entryTime != null && entryTime in startDate..endDate) {
                        val category = entry.category.ifBlank { "Uncategorized" }
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + entry.amount
                    }
                }

                val listAdapter = ArrayAdapter(
                    this@ViewCategoriesActivity,
                    android.R.layout.simple_list_item_1,
                    categoryTotals.map { "${it.key}: R%.2f".format(it.value) }
                )
                //LISTS CATEGORIES AND THERI TOTALS
                categoryListView.adapter = listAdapter

                //GENERATES PIE CHART AND ITS COLOURS
                val pieEntries = categoryTotals.map { PieEntry(it.value.toFloat(), it.key) }
                val pieDataSet = PieDataSet(pieEntries, "Category Spending")
                pieDataSet.colors = listOf(
                    Color.parseColor("#FF5722"),
                    Color.parseColor("#4CAF50"),
                    Color.parseColor("#03A9F4"),
                    Color.parseColor("#FFC107"),
                    Color.parseColor("#E91E63"),
                    Color.parseColor("#9C27B0")
                )

                val pieData = PieData(pieDataSet)
                pieData.setValueTextColor(Color.WHITE)
                pieData.setValueTextSize(14f)

                pieChart.data = pieData
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(true)
                pieChart.setEntryLabelColor(Color.WHITE)
                pieChart.setEntryLabelTextSize(12f)
                pieChart.animateY(1000)
                pieChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ViewCategoriesActivity,
                    "Error loading data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}