package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmadness.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

/**
 * welcom screen after login
 */
class StarterPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter_page)

        showUpcomingPaymentsAlert()

        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            val intent = Intent(this, HomeScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    /**
     * Checks Firebase for any expenses (bills) with a due date within the next [daysAhead] days.
     * If any exist, an alert dialog is shown listing those payments.
     * */
    private fun showUpcomingPaymentsAlert(daysAhead: Int = 5) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        //calculate date range: today to day ahead in the future
        val today = Calendar.getInstance().time
        val future = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysAhead) }.time
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        //reference to users expenses in firebase
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/expenses")
        //fetch all expenses from firebase
        ref.get().addOnSuccessListener { snapshot ->
            val upcoming = snapshot.children.mapNotNull { it.getValue(Expense::class.java) }
                .filter {
                    it.date != null && try {
                        val parsed = formatter.parse(it.date)
                        parsed != null && parsed.after(today) && !parsed.after(future)
                    } catch (_: Exception) { false }
                }

            if (upcoming.isNotEmpty()) {
                //format the list of upcoming expenses for display
                val billList = upcoming.sortedBy { it.date }.joinToString("\n") {
                    "• ${it.name} - ${it.date} (R${"%.2f".format(it.amount)})"
                }
                //show allert dialog with payment summary
                AlertDialog.Builder(this)
                    .setTitle("Upcoming Payments")
                    .setMessage("You have bills due soon:\n\n$billList")
                    .setPositiveButton("Got it", null)
                    .show()
            } else {
                //  show a welcome message if no bills are found
                AlertDialog.Builder(this)
                    .setTitle("Welcome to Budget Madness")
                    .setMessage("No payments are due in the next $daysAhead days. You're all caught up!")
                    .setPositiveButton("Let's Go", null)
                    .show()
            }
        }
    }
}