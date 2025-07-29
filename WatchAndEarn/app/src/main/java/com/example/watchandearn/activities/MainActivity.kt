package com.example.watchandearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchandearn.R
import com.example.watchandearn.databinding.ActivityMainBinding
import com.example.watchandearn.managers.RewardedAdManager
import com.example.watchandearn.utils.Constants
import com.example.watchandearn.utils.FirebaseHelper

class MainActivity : AppCompatActivity(), RewardedAdManager.AdRewardCallback {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var rewardedAdManager: RewardedAdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        firebaseHelper = FirebaseHelper(this)
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            navigateToAuth()
            return
        }
        
        // Initialize AdMob
        rewardedAdManager = RewardedAdManager(this)
        
        setupUI()
        setupClickListeners()
        loadUserData()
    }
    
    override fun onResume() {
        super.onResume()
        loadUserData()
        updateDailyBonusButton()
    }
    
    private fun setupUI() {
        // Set welcome message
        val userEmail = auth.currentUser?.email
        val displayName = userEmail?.substringBefore("@") ?: "User"
        binding.tvWelcome.text = "Welcome back, $displayName!"
    }
    
    private fun setupClickListeners() {
        binding.btnWatchAd.setOnClickListener {
            showRewardedAd()
        }
        
        binding.btnDailyBonus.setOnClickListener {
            claimDailyBonus()
        }
        
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        
        binding.btnWithdrawal.setOnClickListener {
            startActivity(Intent(this, WithdrawalActivity::class.java))
        }
        
        binding.btnReferral.setOnClickListener {
            startActivity(Intent(this, ReferralActivity::class.java))
        }
    }
    
    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        showLoading(true)
        firebaseHelper.getCoinBalance(userId) { balance ->
            showLoading(false)
            updateCoinBalance(balance)
        }
    }
    
    private fun updateCoinBalance(balance: Long) {
        binding.tvCoinBalance.text = "$balance coins"
        val usdValue = firebaseHelper.formatCoinsToUSD(balance)
        binding.tvUsdValue.text = "≈ $usdValue"
    }
    
    private fun updateDailyBonusButton() {
        val canClaim = firebaseHelper.canClaimDailyBonus()
        binding.btnDailyBonus.isEnabled = canClaim
        binding.btnDailyBonus.text = if (canClaim) {
            getString(R.string.daily_bonus_button)
        } else {
            "Daily Bonus Claimed"
        }
    }
    
    private fun showRewardedAd() {
        if (rewardedAdManager.isAdReady()) {
            binding.btnWatchAd.isEnabled = false
            rewardedAdManager.showRewardedAd(this, this)
        } else {
            Toast.makeText(this, getString(R.string.ad_not_ready), Toast.LENGTH_SHORT).show()
            // Try to load ad
            rewardedAdManager.loadRewardedAd(this)
        }
    }
    
    private fun claimDailyBonus() {
        val userId = auth.currentUser?.uid ?: return
        
        if (!firebaseHelper.canClaimDailyBonus()) {
            Toast.makeText(this, getString(R.string.daily_bonus_already_claimed), Toast.LENGTH_SHORT).show()
            return
        }
        
        showLoading(true)
        firebaseHelper.claimDailyBonus(userId) { success ->
            showLoading(false)
            if (success) {
                Toast.makeText(this, getString(R.string.daily_bonus_claimed), Toast.LENGTH_SHORT).show()
                loadUserData()
                updateDailyBonusButton()
            } else {
                Toast.makeText(this, "Failed to claim daily bonus", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    // AdRewardCallback implementations
    override fun onAdLoaded() {
        runOnUiThread {
            binding.btnWatchAd.isEnabled = true
            binding.btnWatchAd.text = getString(R.string.watch_ad_button)
        }
    }
    
    override fun onAdFailedToLoad(error: String) {
        runOnUiThread {
            binding.btnWatchAd.isEnabled = true
            binding.btnWatchAd.text = getString(R.string.watch_ad_button)
            Toast.makeText(this, getString(R.string.ad_failed), Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onAdShown() {
        runOnUiThread {
            binding.btnWatchAd.text = "Watching Ad..."
        }
    }
    
    override fun onAdDismissed() {
        runOnUiThread {
            binding.btnWatchAd.isEnabled = true
            binding.btnWatchAd.text = getString(R.string.watch_ad_button)
        }
    }
    
    override fun onUserEarnedReward(coins: Int) {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.coins_earned), Toast.LENGTH_SHORT).show()
            loadUserData() // Refresh balance
            binding.btnWatchAd.isEnabled = true
            binding.btnWatchAd.text = getString(R.string.watch_ad_button)
        }
    }
    
    override fun onAdFailedToShow(error: String) {
        runOnUiThread {
            binding.btnWatchAd.isEnabled = true
            binding.btnWatchAd.text = getString(R.string.watch_ad_button)
            Toast.makeText(this, "Failed to show ad: $error", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        rewardedAdManager.destroy()
    }
}
