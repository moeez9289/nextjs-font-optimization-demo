package com.example.watchandearn.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchandearn.R
import com.example.watchandearn.databinding.ActivityAuthBinding
import com.example.watchandearn.utils.FirebaseHelper

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private var isLoginMode = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        firebaseHelper = FirebaseHelper(this)
        
        // Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }
        
        setupUI()
        setupClickListeners()
    }
    
    private fun setupUI() {
        updateUIForMode()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performSignUp()
            }
        }
        
        binding.btnSignUp.setOnClickListener {
            if (isLoginMode) {
                performSignUp()
            } else {
                performLogin()
            }
        }
        
        binding.tvSwitchMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUIForMode()
        }
    }
    
    private fun updateUIForMode() {
        if (isLoginMode) {
            binding.btnLogin.text = getString(R.string.login_button)
            binding.btnSignUp.text = getString(R.string.signup_button)
            binding.tvSwitchMode.text = getString(R.string.switch_to_signup)
        } else {
            binding.btnLogin.text = getString(R.string.signup_button)
            binding.btnSignUp.text = getString(R.string.login_button)
            binding.tvSwitchMode.text = getString(R.string.switch_to_login)
        }
    }
    
    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        if (!validateInput(email, password)) return
        
        showLoading(true)
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val errorMessage = task.exception?.message ?: "Login failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun performSignUp() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        if (!validateInput(email, password)) return
        
        showLoading(true)
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Create user profile in Firestore
                    val user = auth.currentUser
                    if (user != null) {
                        val displayName = email.substringBefore("@")
                        firebaseHelper.createUserProfile(
                            user.uid,
                            email,
                            displayName
                        ) { success ->
                            showLoading(false)
                            if (success) {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                navigateToMain()
                            } else {
                                Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Failed to create account", Toast.LENGTH_LONG).show()
                    }
                } else {
                    showLoading(false)
                    val errorMessage = task.exception?.message ?: "Sign up failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            return false
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        
        // Clear errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        
        return true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnSignUp.isEnabled = !show
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
