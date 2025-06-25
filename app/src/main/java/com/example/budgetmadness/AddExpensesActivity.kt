package com.example.budgetmadness

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.budgetmadness.models.Expense
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddExpenseActivity allows users to input expense data,
 * select a category, attach a receipt image, and store everything in Firebase.
 * Features used: Firebase realtime database, camera/gallery  access, date picker dialog, bottom navigation
 */
class AddExpensesActivity : AppCompatActivity() {

    //UI components
    private lateinit var expenseNameInput: EditText
    private lateinit var expenseAmountInput: EditText
    private lateinit var paymentMethodInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var selectDateButton: Button
    private lateinit var uploadReceiptButton: Button
    private lateinit var addExpenseButton: Button

    //variable to hold selected date
    private var selectedDate: String = ""
    //variable to hold image (receipt image)```
    private var imageUri: Uri? = null
    //request code for intent results
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expenses)

        //this code block links UI components to layout views
        expenseNameInput = findViewById(R.id.expenseNameInput)
        expenseAmountInput = findViewById(R.id.expenseAmountInput)
        paymentMethodInput = findViewById(R.id.paymentMethodInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        selectDateButton = findViewById(R.id.selectDateButton)
        uploadReceiptButton = findViewById(R.id.uploadReceiptButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)

        // Initializes Month dropdown with months inside
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter

        //load dynamic categories from firebase to display in a drop down
        loadCategories()

        //date picker dialog to allow a user to select a day of the year they used an expense on
        selectDateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                selectDateButton.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        //uploads and stores an image of users choice in relation to their expense
        //shows image source options (camera or gallery)
        uploadReceiptButton.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            AlertDialog.Builder(this)
                .setTitle("Choose Image Source")
                .setItems(options) { _, which ->
                    if (which == 0) openCamera() else openGallery()
                }
                .show()
        }

        //captures all inputs and uploads them into the firebase database when clicked
        addExpenseButton.setOnClickListener {
            val name = expenseNameInput.text.toString().trim()
            val amount = expenseAmountInput.text.toString().toDoubleOrNull() ?: 0.0
            val method = paymentMethodInput.text.toString().trim()
            val category = categorySpinner.selectedItem?.toString() ?: "Uncategorized"
            val date = selectedDate
            val month = monthSpinner.selectedItem?.toString() ?: ""
            //if button is clicked and all fields are empty user cannot upload expenses...
            if (name.isEmpty() || method.isEmpty() || date.isEmpty() || month.isEmpty()) {
                //...they are then prompted with a toast message
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //checks the user who is logged in, and then saves the data into their specific account on firebase
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val expense = Expense(name, amount, method, category, date, month, imageUri?.toString())

            //pushes data to realtime database on firebase
            FirebaseDatabase.getInstance()
                .getReference("users/$uid/expenses")
                .push()
                .setValue(expense)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                    clearInputs()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
        }
        //bottom navigation for easy switching between screens
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                //navigates to income screen
                R.id.nav_income -> startActivity(Intent(this, IncomeActivity::class.java))
                //navigates to starter screen
                R.id.nav_home -> startActivity(Intent(this, StarterPageActivity::class.java))
                //navigates to add expenses screen
                R.id.nav_add -> startActivity(Intent(this, AddExpensesActivity::class.java))
                //navigates to open menu (intent menu)
                R.id.nav_open_menu -> startActivity(Intent(this, MenuActivity::class.java))
                else -> false
            }
            true
        }
    }

    //method used earlier in the code to load and display saved categories created by user in category screen
    private fun loadCategories() {
        //checks which user account it is and fetches their data
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/categories")

        //displays it in a dropdown if successful
        ref.get().addOnSuccessListener { snapshot ->
            val categories = mutableListOf<String>()
            for (child in snapshot.children) {
                child.key?.let { categories.add(it) }
            }
            if (categories.isEmpty()) categories.add("Uncategorized")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
    }
    //method used in code earlier to capture images taken by camera if user allows permission
    private fun openCamera() {
        val imageFile = File.createTempFile("receipt_", ".jpg", externalCacheDir)
        imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }
    //method to open gallery if user allows permission
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    //handles the result after the user picks an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK) {
            imageUri = data?.data
        }
    }
    //clears all inputs and fields after successful submission
    private fun clearInputs() {
        expenseNameInput.text.clear()
        expenseAmountInput.text.clear()
        paymentMethodInput.text.clear()
        selectDateButton.text = "Select Date"
        selectedDate = ""
        imageUri = null
    }
}