package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
/**
 * CategoriesActivity allows users to:
 * - View all their expense categories.
 * - Add new categories with optional budget limits.
 * - Delete selected categories.
 *
 * Categories are stored in Firebase under each user's node.
 */

class CategoriesActivity : AppCompatActivity() {
    // UI components

    private lateinit var categoryListView: ListView
    private lateinit var newCategoryInput: EditText
    private lateinit var budgetLimitInput: EditText
    private lateinit var addCategoryButton: Button
    private lateinit var deleteCategoryButton: Button
    private lateinit var adapter: ArrayAdapter<String>
    private val categories = mutableListOf<String>()
    private var selectedCategory: String? = null
    //Data structures
    private lateinit var databaseRef: DatabaseReference
    //firebase set up
    private val uid: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)
        // Initialize UI elements
        categoryListView = findViewById(R.id.categoryListView)
        newCategoryInput = findViewById(R.id.newCategoryInput)
        budgetLimitInput = findViewById(R.id.budgetLimitInput)
        addCategoryButton = findViewById(R.id.addCategoryButton)
        deleteCategoryButton = findViewById(R.id.deleteCategoryButton)
        // Adapter to display categories in a simple list
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        categoryListView.adapter = adapter
        // Set reference to Firebase categories node for the
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid ?: "")
            .child("categories")
        // Load categories from Firebase when activity opens
        loadCategories()
        // When an item in the list is tapped, mark it as selected
        categoryListView.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
            Toast.makeText(this, "$selectedCategory selected", Toast.LENGTH_SHORT).show()
        }
        // Add a new category with optional budget limit
        addCategoryButton.setOnClickListener {
            val newCategory = newCategoryInput.text.toString().trim()
            val budgetLimit = budgetLimitInput.text.toString().toDoubleOrNull() ?: 0.0

            if (newCategory.isNotEmpty()) {
                val categoryData = mapOf("budget_limit" to budgetLimit)

                databaseRef.child(newCategory).setValue(categoryData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category added with limit R$budgetLimit", Toast.LENGTH_SHORT).show()
                        newCategoryInput.text.clear()
                        budgetLimitInput.text.clear()
                        loadCategories()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Enter a valid category", Toast.LENGTH_SHORT).show()
            }
        }
        // Delete the selected category
        deleteCategoryButton.setOnClickListener {
            selectedCategory?.let { category ->
                databaseRef.child(category).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "$category deleted!", Toast.LENGTH_SHORT).show()
                        loadCategories()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(this, "Select a category to delete", Toast.LENGTH_SHORT).show()
            }
        }
        // Setup bottom navigation bar to navigate between major sections of the app
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener { item ->
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
    /**
     * Fetches the category list from Firebase and updates the UI.
     */
    private fun loadCategories() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                for (child in snapshot.children) {
                    child.key?.let { categories.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CategoriesActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }
}