package com.example.watchandearn.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchandearn.R
import com.example.watchandearn.databinding.ActivityWithdrawalBinding
import com.example.watchandearn.utils.Constants
import com.example.watchandearn.utils.FirebaseHelper

class WithdrawalActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWithdrawalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private var currentBalance: Long = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawalBinding.inflate(layoutInflater)
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
        setupTextWatchers()
        loadCurrentBalance()
    }
    
    private fun setupClickListeners() {
        binding.btnSubmitWithdrawal.setOnClickListener {
            submitWithdrawalRequest()
        }
    }
    
    private fun setupTextWatchers() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                updateUsdValue()
            }
        })
    }
    
    private fun loadCurrentBalance() {
        val userId = auth.currentUser?.uid ?: return
        
        showLoading(true)
        firebaseHelper.getCoinBalance(userId) { balance ->
            showLoading(false)
            currentBalance = balance
            binding.tvCurrentBalance.text = "$balance coins"
        }
    }
    
    private fun updateUsdValue() {
        val amountText = binding.etAmount.text.toString()
        if (amountText.isNotEmpty()) {
            try {
                val coins = amountText.toLong()
                val usdValue = firebaseHelper.formatCoinsToUSD(coins)
                binding.tvUsdValue.text = "USD Value: $usdValue"
            } catch (e: NumberFormatException) {
                binding.tvUsdValue.text = "USD Value: $0.00"
            }
        } else {
            binding.tvUsdValue.text = "USD Value: $0.00"
        }
    }
    
    private fun submitWithdrawalRequest() {
        val walletId = binding.etWalletId.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        
        if (!validateInput(walletId, amountText)) return
        
        val coinAmount = amountText.toInt()
        val userId = auth.currentUser?.uid ?: return
        
        showLoading(true)
        firebaseHelper.submitWithdrawalRequest(userId, walletId, coinAmount) { success ->
            showLoading(false)
            if (success) {
                Toast.makeText(this, getString(R.string.withdrawal_submitted), Toast.LENGTH_LONG).show()
                // Clear form
                binding.etWalletId.setText("")
                binding.etAmount.setText("")
                // Reload balance
                loadCurrentBalance()
            } else {
                Toast.makeText(this, "Failed to submit withdrawal request", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun validateInput(walletId: String, amountText: String): Boolean {
        // Validate wallet ID
        if (walletId.isEmpty()) {
            binding.tilWalletId.error = "Wallet ID is required"
            return false
        }
        
        if (walletId.length < 5) {
            binding.tilWalletId.error = "Please enter a valid wallet ID or PayPal email"
            return false
        }
        
        // Validate amount
        if (amountText.isEmpty()) {
            binding.tilAmount.error = "Amount is required"
            return false
        }
        
        val coinAmount = try {
            amountText.toInt()
        } catch (e: NumberFormatException) {
            binding.tilAmount.error = "Please enter a valid number"
            return false
        }
        
        if (coinAmount < Constants.MIN_WITHDRAWAL_COINS) {
            binding.tilAmount.error = getString(R.string.minimum_withdrawal)
            return false
        }
        
        if (coinAmount > currentBalance) {
            binding.tilAmount.error = getString(R.string.insufficient_coins)
            return false
        }
        
        // Clear errors
        binding.tilWalletId.error = null
        binding.tilAmount.error = null
        
        return true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSubmitWithdrawal.isEnabled = !show
    }
}
