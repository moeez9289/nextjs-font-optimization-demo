package com.example.watchandearn.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.example.watchandearn.utils.Constants
import com.example.watchandearn.utils.FirebaseHelper

class RewardedAdManager(private val context: Context) {
    
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val firebaseHelper = FirebaseHelper(context)
    
    companion object {
        private const val TAG = "RewardedAdManager"
    }
    
    interface AdRewardCallback {
        fun onAdLoaded()
        fun onAdFailedToLoad(error: String)
        fun onAdShown()
        fun onAdDismissed()
        fun onUserEarnedReward(coins: Int)
        fun onAdFailedToShow(error: String)
    }
    
    init {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob SDK initialized: ${initializationStatus.adapterStatusMap}")
            loadRewardedAd()
        }
    }
    
    fun loadRewardedAd(callback: AdRewardCallback? = null) {
        if (isLoading) {
            Log.d(TAG, "Ad is already loading")
            return
        }
        
        if (rewardedAd != null) {
            Log.d(TAG, "Ad is already loaded")
            callback?.onAdLoaded()
            return
        }
        
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            Constants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Failed to load rewarded ad: ${adError.message}")
                    rewardedAd = null
                    isLoading = false
                    callback?.onAdFailedToLoad(adError.message)
                }
                
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isLoading = false
                    callback?.onAdLoaded()
                    
                    // Set full screen content callback
                    setupAdCallbacks(ad, callback)
                }
            }
        )
    }
    
    private fun setupAdCallbacks(ad: RewardedAd, callback: AdRewardCallback?) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked")
            }
            
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content")
                rewardedAd = null
                callback?.onAdDismissed()
                // Load next ad
                loadRewardedAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content: ${adError.message}")
                rewardedAd = null
                callback?.onAdFailedToShow(adError.message)
                // Load next ad
                loadRewardedAd()
            }
            
            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression")
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content")
                callback?.onAdShown()
            }
        }
    }
    
    fun showRewardedAd(activity: Activity, callback: AdRewardCallback) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                // User earned reward
                val rewardAmount = Constants.REWARDED_AD_COINS
                Log.d(TAG, "User earned reward: $rewardAmount coins")
                
                // Add coins to user account
                val userId = firebaseHelper.getCurrentUserId()
                if (userId != null) {
                    firebaseHelper.addCoins(
                        userId, 
                        rewardAmount, 
                        Constants.TRANSACTION_AD_WATCH
                    ) { success ->
                        if (success) {
                            callback.onUserEarnedReward(rewardAmount)
                        } else {
                            Log.e(TAG, "Failed to add coins to user account")
                            callback.onAdFailedToShow("Failed to process reward")
                        }
                    }
                } else {
                    Log.e(TAG, "User not authenticated")
                    callback.onAdFailedToShow("User not authenticated")
                }
            }
        } else {
            Log.e(TAG, "Rewarded ad is not ready")
            callback.onAdFailedToShow("Ad not ready")
            // Try to load ad
            loadRewardedAd(callback)
        }
    }
    
    fun isAdReady(): Boolean {
        return rewardedAd != null
    }
    
    fun preloadAd() {
        if (rewardedAd == null && !isLoading) {
            loadRewardedAd()
        }
    }
    
    // Call this when activity is destroyed
    fun destroy() {
        rewardedAd = null
        isLoading = false
    }
}
