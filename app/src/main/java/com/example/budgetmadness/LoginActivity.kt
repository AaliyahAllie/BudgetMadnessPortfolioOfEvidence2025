package com.example.budgetmadness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.budgetmadness.models.User
/**
 * Activity that handles user login using Firebase Authentication.
 * Verifies credentials, retrieves user data from Firebase Realtime Database,
 * and navigates to the home screen on successful login.
 */

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //firebase setup
        auth = FirebaseAuth.getInstance()
        //links ui to layout
        val username = findViewById<EditText>(R.id.loginEmail)
        val password = findViewById<EditText>(R.id.loginPassword)
        val loginBtn = findViewById<Button>(R.id.loginBtnFinal)

        //when login button is clicked it reads email and password based on firebase and logs in if user is on the database
        loginBtn.setOnClickListener {
            val emailOrUsername = username.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailOrUsername.isNotEmpty() && passwordText.isNotEmpty()) {
                //attempts login using firebase authentication
                auth.signInWithEmailAndPassword(emailOrUsername, passwordText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                //fetches user details from Realtime Database
                                FirebaseDatabase.getInstance().getReference("users")
                                    .child(uid)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val user = snapshot.getValue(User::class.java)
                                        Toast.makeText(this, "Welcome ${user?.firstName}", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, StarterPageActivity::class.java))
                                        finish()
                                    }
                            }
                            //toast message if login is successful
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                //if user press login without details login gives a message
                Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}