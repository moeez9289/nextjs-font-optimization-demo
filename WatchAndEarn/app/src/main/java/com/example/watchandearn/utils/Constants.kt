package com.example.watchandearn.utils

object Constants {
    // Coin rewards
    const val REWARDED_AD_COINS = 30
    const val DAILY_BONUS_COINS = 10
    const val REFERRAL_BONUS_COINS = 200
    
    // Conversion rates
    const val COINS_PER_DOLLAR = 10000 // 10000 coins = $1.00
    const val MIN_WITHDRAWAL_COINS = 1000 // Minimum 1000 coins = $0.10
    
    // Revenue split
    const val VIEWER_PERCENTAGE = 30 // 30% for viewers
    const val OWNER_PERCENTAGE = 70 // 70% for owner
    
    // AdMob Test IDs (Replace with your actual IDs in production)
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val TRANSACTIONS_COLLECTION = "transactions"
    const val WITHDRAWAL_REQUESTS_COLLECTION = "withdrawalRequests"
    
    // Transaction Types
    const val TRANSACTION_AD_WATCH = "ad_watch"
    const val TRANSACTION_DAILY_BONUS = "daily_bonus"
    const val TRANSACTION_REFERRAL_BONUS = "referral_bonus"
    const val TRANSACTION_WITHDRAWAL = "withdrawal"
    
    // Withdrawal Status
    const val WITHDRAWAL_PENDING = "pending"
    const val WITHDRAWAL_APPROVED = "approved"
    const val WITHDRAWAL_REJECTED = "rejected"
    
    // Shared Preferences Keys
    const val PREFS_NAME = "WatchAndEarnPrefs"
    const val PREF_LAST_DAILY_BONUS = "last_daily_bonus"
    const val PREF_USER_REFERRAL_CODE = "user_referral_code"
    
    // Date format
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
}
