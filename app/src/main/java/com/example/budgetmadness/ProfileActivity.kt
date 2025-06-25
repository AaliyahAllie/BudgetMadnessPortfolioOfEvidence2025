package com.example.budgetmadness

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.InputStream
/**
 * ProfileActivity allows users to view and update their profile information,
 * including first name, last name, email, phone number, and profile image.
 *
 * This fulfills Feature 6: "Profile Screen" as outlined in the readme file*/
class ProfileActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    //UI COMPONENTS
    private lateinit var profileImage: ImageView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText

    private val TAG = "ProfileActivity"
    //FIREBASE SETUP
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImage = findViewById(R.id.profileImage)
        val btnChangeImage = findViewById<Button>(R.id.btnChangeImage)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)

        //CALLS METHOD
        loadUserProfile()

        //ALLOWS USER TO UPLOAD AN IMAGE TO THEIR PROFILE
        btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        //ALLOWS USER TO SAVE CHANGES
        btnSave.setOnClickListener {
            saveUserProfile()
        }
        //SETS UP NAVIGATION OF SCREENS
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
    //LOADS USERS INFORMATION BASED ON WHATS IN FIREBASE
    private fun loadUserProfile() {
        val userId = uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$userId")

        //DISPLAYS INFORMATION OF USER AND CAN BE UPDATED IF THEY WISH TO DO SO
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                etFirstName.setText(snapshot.child("firstName").getValue(String::class.java))
                etLastName.setText(snapshot.child("lastName").getValue(String::class.java))
                etEmail.setText(snapshot.child("email").getValue(String::class.java))
                etPhone.setText(snapshot.child("phone").getValue(String::class.java))

                //CAPTURES IMAGE
                val imageUri = snapshot.child("imageUri").getValue(String::class.java)
                if (!imageUri.isNullOrEmpty()) {
                    try {
                        val inputStream = contentResolver.openInputStream(Uri.parse(imageUri))
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        profileImage.setImageBitmap(bitmap)
                        selectedImageUri = Uri.parse(imageUri)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load saved profile image", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load profile: ${error.message}")
            }
        })
    }

    //SAVES PROFILE CHANGES TO FIREBASE
    private fun saveUserProfile() {
        val userId = uid ?: return
        val userMap = mapOf(
            "firstName" to etFirstName.text.toString().trim(),
            "lastName" to etLastName.text.toString().trim(),
            "email" to etEmail.text.toString().trim(),
            "phone" to etPhone.text.toString().trim(),
            "imageUri" to (selectedImageUri?.toString() ?: "")
        )

        FirebaseDatabase.getInstance().getReference("users/$userId")
            .updateChildren(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Update failed: ${it.message}")
            }
    }

    //CALLED WHEN USER RETURNS THE IMAGE PICKER
    //DISPLAYS THE SELECTED IMAGE AND STORES ITS URI
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}