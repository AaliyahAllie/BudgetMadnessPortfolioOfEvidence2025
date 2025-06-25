package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmadness.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
/**
 * RegisterActivity allows users to create a new account by providing their personal details.
 * Information is saved to Firebase Authentication and Realtime Database.
 *
 * This implements Feature 2: "Register Screen" from the readme file
*/
class RegisterActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Authenticates firebase instance
        auth = FirebaseAuth.getInstance()

        //links ui and layout view
        val username = findViewById<EditText>(R.id.usernameInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val firstName = findViewById<EditText>(R.id.firstNameInput)
        val lastName = findViewById<EditText>(R.id.lastNameInput)
        val email = findViewById<EditText>(R.id.emailInput)
        val phone = findViewById<EditText>(R.id.phoneInput)
        val registerBtn = findViewById<Button>(R.id.registerUserBtn)

        //Handle register button click
        registerBtn.setOnClickListener {
            val userText = username.text.toString().trim()
            val passText = password.text.toString().trim()
            val emailText = email.text.toString().trim()
            val firstText = firstName.text.toString().trim()
            val lastText = lastName.text.toString().trim()
            val phoneText = phone.text.toString().trim()

            //validates that all fields are filled
            if (userText.isNotEmpty() && passText.isNotEmpty() &&
                emailText.isNotEmpty() && firstText.isNotEmpty() &&
                lastText.isNotEmpty() && phoneText.isNotEmpty()
            ) {
                //creates a user in firebase authentication
                auth.createUserWithEmailAndPassword(emailText, passText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //gets unique id form firebase auth or fallback to username
                            val uid = auth.currentUser?.uid ?: userText
                            //create a user data object
                            val user = User(userText, passText, firstText, lastText, emailText, phoneText)
                            //saves user into realtime database
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .setValue(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                        } else {
                            //toast message if unsuccessful
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                //tells user to add data into fields if they click and its empty
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
