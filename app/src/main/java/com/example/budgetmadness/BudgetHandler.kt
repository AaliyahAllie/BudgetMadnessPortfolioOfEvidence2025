package com.example.budgetmadness

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.budgetmadness.models.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Manages local and remote budget data operations
 * stores montly min and max locally
 * uploads bydget data to Firebase under authenticated user node
 */
class BudgetHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        /**
         * creates local SQLite table for storing budgets.
         */
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MONTH TEXT,
                $COLUMN_MIN REAL,
                $COLUMN_MAX REAL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }
    //called when the database version changes, drops the old table and recreates it.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
    //inserts and saves it to the datbase
    fun insertBudget(month: String, min: Double, max: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MONTH, month)
            put(COLUMN_MIN, min)
            put(COLUMN_MAX, max)
        }
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L
    }
    //uploads it to firebase
    fun uploadBudgetToFirebase(month: String, min: Double, max: Double) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val budgetEntry = Budget(month, min, max)
        FirebaseDatabase.getInstance()
            .getReference("users/$uid/budgets/$month")
            .setValue(budgetEntry)
            .addOnSuccessListener {
                Log.d("BudgetHandler", "Budget uploaded to Firebase for $month")
            }
            .addOnFailureListener { e ->
                Log.e("BudgetHandler", "Firebase upload failed: ${e.message}")
            }
    }
    //function to get the months to display in a drop down
    fun getBudgetForMonth(month: String): Budget? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_MONTH, COLUMN_MIN, COLUMN_MAX),
            "$COLUMN_MONTH = ?",
            arrayOf(month),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            val min = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_MIN))
            val max = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_MAX))
            cursor.close()
            Budget(month, min, max)
        } else {
            cursor.close()
            null
        }
    }

    companion object {
        private const val DATABASE_NAME = "budget.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_NAME = "budget"
        private const val COLUMN_ID = "id"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_MIN = "min_budget"
        private const val COLUMN_MAX = "max_budget"
    }
}