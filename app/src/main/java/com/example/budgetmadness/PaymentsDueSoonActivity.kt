package com.example.budgetmadness

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
/**
 * PaymentsDueSoonActivity allows users to input upcoming payments they want to be reminded of.
 *
 * This supports Feature 15 in the README: "Payments Due Soon" functionality,
 * which feeds into login notifications if a due date is approaching.
 */
class PaymentsDueSoonActivity : AppCompatActivity() {
    // UI components
    private lateinit var titleEdit: EditText
    private lateinit var amountEdit: EditText
    private lateinit var dueDateEdit: EditText
    private lateinit var saveButton: Button
    // Format for storing/displaying dates
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments_due_soon)
        // Initialize input fields
        titleEdit = findViewById(R.id.editPaymentTitle)
        amountEdit = findViewById(R.id.editPaymentAmount)
        dueDateEdit = findViewById(R.id.editPaymentDueDate)
        saveButton = findViewById(R.id.buttonSavePayment)
        // Open a date picker when the user clicks the due date field
        dueDateEdit.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                dueDateEdit.setText(formatted)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        // Save payment data to Firebase on button click
        saveButton.setOnClickListener {
            val title = titleEdit.text.toString()
            val amount = amountEdit.text.toString()
            val dueDate = dueDateEdit.text.toString()
            // Basic validation: make sure all fields are filled
            if (title.isEmpty() || amount.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Get the currently logged-in user's UID
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val paymentsRef = FirebaseDatabase.getInstance().getReference("users/$uid/payments")
            val paymentId = paymentsRef.push().key ?: return@setOnClickListener
            // Structure of the payment entry
            val paymentData = mapOf(
                "title" to title,
                "amount" to amount,
                "dueDate" to dueDate
            )
            // Save the payment to Firebase and notify the user on success
            paymentsRef.child(paymentId).setValue(paymentData).addOnSuccessListener {
                Toast.makeText(this, "Payment saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}