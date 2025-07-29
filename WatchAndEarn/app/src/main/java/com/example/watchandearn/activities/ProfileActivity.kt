package com.example.watchandearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchandearn.R
import com.example.watchandearn.databinding.ActivityProfileBinding
import com.example.watchandearn.utils.FirebaseHelper

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        firebaseHelper = FirebaseHelper(this)
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            navigateToAuth()
            return
        }
        
        setupClickListeners()
        loadUserProfile()
    }
    
    private fun setupClickListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
        
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""
        
        // Set email (read-only)
        binding.tvEmail.text = userEmail
        
        showLoading(true)
        firebaseHelper.getUserProfile(userId) { userData ->
            showLoading(false)
            if (userData != null) {
                val displayName = userData["displayName"] as? String ?: ""
                binding.etDisplayName.setText(displayName)
            } else {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateProfile() {
        val displayName = binding.etDisplayName.text.toString().trim()
        
        if (displayName.isEmpty()) {
            binding.tilDisplayName.error = "Display name is required"
            return
        }
        
        if (displayName.length < 2) {
            binding.tilDisplayName.error = "Display name must be at least 2 characters"
            return
        }
        
        binding.tilDisplayName.error = null
        
        val userId = auth.currentUser?.uid ?: return
        
        showLoading(true)
        firebaseHelper.updateUserProfile(userId, displayName) { success ->
            showLoading(false)
            if (success) {
                Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        auth.signOut()
        navigateToAuth()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnUpdateProfile.isEnabled = !show
    }
    
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
