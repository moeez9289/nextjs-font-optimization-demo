package com.example.watchandearn.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchandearn.R
import com.example.watchandearn.databinding.ActivityReferralBinding
import com.example.watchandearn.utils.FirebaseHelper

class ReferralActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReferralBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private var referralCode: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferralBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        firebaseHelper = FirebaseHelper(this)
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            finish()
            return
        }
        
        setupClickListeners()
        loadReferralData()
    }
    
    private fun setupClickListeners() {
        binding.btnCopyCode.setOnClickListener {
            copyReferralCode()
        }
        
        binding.btnShareReferral.setOnClickListener {
            shareReferralCode()
        }
    }
    
    private fun loadReferralData() {
        val userId = auth.currentUser?.uid ?: return
        
        firebaseHelper.getUserProfile(userId) { userData ->
            if (userData != null) {
                referralCode = userData["referralCode"] as? String ?: ""
                binding.tvReferralCode.text = referralCode
                
                // Load referral stats (you can implement this in FirebaseHelper)
                loadReferralStats()
            }
        }
    }
    
    private fun loadReferralStats() {
        // For now, show placeholder values
        // In a real implementation, you would query Firestore for referral statistics
        binding.tvTotalReferrals.text = "0"
        binding.tvReferralEarnings.text = "0"
    }
    
    private fun copyReferralCode() {
        if (referralCode.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Referral Code", referralCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Referral code copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareReferralCode() {
        if (referralCode.isNotEmpty()) {
            val shareText = getString(R.string.share_text, referralCode)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Join Watch & Earn!")
            }
            
            startActivity(Intent.createChooser(shareIntent, "Share Referral Code"))
        }
    }
}
