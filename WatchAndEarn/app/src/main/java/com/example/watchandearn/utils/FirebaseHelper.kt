package com.example.watchandearn.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class FirebaseHelper(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
    
    // User Management
    fun createUserProfile(userId: String, email: String, displayName: String, callback: (Boolean) -> Unit) {
        val referralCode = generateReferralCode()
        val userData = hashMapOf(
            "email" to email,
            "displayName" to displayName,
            "coinBalance" to 0,
            "referralCode" to referralCode,
            "createdAt" to System.currentTimeMillis(),
            "totalEarnings" to 0.0
        )
        
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                // Save referral code to preferences
                prefs.edit().putString(Constants.PREF_USER_REFERRAL_CODE, referralCode).apply()
                callback(true)
            }
            .addOnFailureListener { callback(false) }
    }
    
    fun getUserProfile(userId: String, callback: (Map<String, Any>?) -> Unit) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(document.data)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { callback(null) }
    }
    
    fun updateUserProfile(userId: String, displayName: String, callback: (Boolean) -> Unit) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .update("displayName", displayName)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
    
    // Coin Management
    fun addCoins(userId: String, coins: Int, transactionType: String, callback: (Boolean) -> Unit) {
        val userRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentBalance = snapshot.getLong("coinBalance") ?: 0
            val newBalance = currentBalance + coins
            
            // Update user balance
            transaction.update(userRef, "coinBalance", newBalance)
            
            // Add transaction record
            val transactionData = hashMapOf(
                "userId" to userId,
                "type" to transactionType,
                "amount" to coins,
                "timestamp" to System.currentTimeMillis(),
                "description" to getTransactionDescription(transactionType, coins)
            )
            
            val transactionRef = firestore.collection(Constants.TRANSACTIONS_COLLECTION).document()
            transaction.set(transactionRef, transactionData)
            
            newBalance
        }.addOnSuccessListener { callback(true) }
         .addOnFailureListener { callback(false) }
    }
    
    fun getCoinBalance(userId: String, callback: (Long) -> Unit) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val balance = document.getLong("coinBalance") ?: 0
                callback(balance)
            }
            .addOnFailureListener { callback(0) }
    }
    
    // Daily Bonus Management
    fun canClaimDailyBonus(): Boolean {
        val lastClaimDate = prefs.getString(Constants.PREF_LAST_DAILY_BONUS, "")
        val today = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
        return lastClaimDate != today
    }
    
    fun claimDailyBonus(userId: String, callback: (Boolean) -> Unit) {
        if (!canClaimDailyBonus()) {
            callback(false)
            return
        }
        
        addCoins(userId, Constants.DAILY_BONUS_COINS, Constants.TRANSACTION_DAILY_BONUS) { success ->
            if (success) {
                val today = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())
                prefs.edit().putString(Constants.PREF_LAST_DAILY_BONUS, today).apply()
            }
            callback(success)
        }
    }
    
    // Transaction History
    fun getTransactionHistory(userId: String, callback: (List<Map<String, Any>>) -> Unit) {
        firestore.collection(Constants.TRANSACTIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { documents ->
                val transactions = documents.map { it.data }
                callback(transactions)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
    
    // Withdrawal Management
    fun submitWithdrawalRequest(userId: String, walletId: String, coinAmount: Int, callback: (Boolean) -> Unit) {
        if (coinAmount < Constants.MIN_WITHDRAWAL_COINS) {
            callback(false)
            return
        }
        
        getCoinBalance(userId) { currentBalance ->
            if (currentBalance >= coinAmount) {
                val dollarAmount = coinAmount.toDouble() / Constants.COINS_PER_DOLLAR
                val withdrawalData = hashMapOf(
                    "userId" to userId,
                    "walletId" to walletId,
                    "coinAmount" to coinAmount,
                    "dollarAmount" to dollarAmount,
                    "status" to Constants.WITHDRAWAL_PENDING,
                    "requestDate" to System.currentTimeMillis(),
                    "processedDate" to null
                )
                
                firestore.collection(Constants.WITHDRAWAL_REQUESTS_COLLECTION)
                    .add(withdrawalData)
                    .addOnSuccessListener {
                        // Deduct coins from user balance
                        val userRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
                        userRef.update("coinBalance", currentBalance - coinAmount)
                        
                        // Add transaction record
                        addCoins(userId, -coinAmount, Constants.TRANSACTION_WITHDRAWAL) { }
                        callback(true)
                    }
                    .addOnFailureListener { callback(false) }
            } else {
                callback(false)
            }
        }
    }
    
    // Referral System
    fun getReferralCode(userId: String): String {
        return prefs.getString(Constants.PREF_USER_REFERRAL_CODE, "") ?: ""
    }
    
    fun processReferral(referralCode: String, newUserId: String, callback: (Boolean) -> Unit) {
        // Find user with this referral code
        firestore.collection(Constants.USERS_COLLECTION)
            .whereEqualTo("referralCode", referralCode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val referrerDoc = documents.first()
                    val referrerId = referrerDoc.id
                    
                    // Add bonus to referrer
                    addCoins(referrerId, Constants.REFERRAL_BONUS_COINS, Constants.TRANSACTION_REFERRAL_BONUS) { success ->
                        callback(success)
                    }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
    }
    
    // Helper Methods
    private fun generateReferralCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    private fun getTransactionDescription(type: String, amount: Int): String {
        return when (type) {
            Constants.TRANSACTION_AD_WATCH -> "Earned $amount coins by watching ad"
            Constants.TRANSACTION_DAILY_BONUS -> "Daily bonus: $amount coins"
            Constants.TRANSACTION_REFERRAL_BONUS -> "Referral bonus: $amount coins"
            Constants.TRANSACTION_WITHDRAWAL -> "Withdrawal request: $amount coins"
            else -> "Transaction: $amount coins"
        }
    }
    
    // Format currency
    fun formatCoinsToUSD(coins: Long): String {
        val dollars = coins.toDouble() / Constants.COINS_PER_DOLLAR
        return String.format("$%.2f", dollars)
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.getDefault())
        return formatter.format(date)
    }
}
