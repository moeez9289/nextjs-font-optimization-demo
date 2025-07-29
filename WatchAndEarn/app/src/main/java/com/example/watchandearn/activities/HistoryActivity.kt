package com.example.watchandearn.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.watchandearn.R
import com.example.watchandearn.adapters.TransactionAdapter
import com.example.watchandearn.databinding.ActivityHistoryBinding
import com.example.watchandearn.utils.FirebaseHelper

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var transactionAdapter: TransactionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        firebaseHelper = FirebaseHelper(this)
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            finish()
            return
        }
        
        setupRecyclerView()
        loadTransactionHistory()
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(firebaseHelper)
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = transactionAdapter
        }
    }
    
    private fun loadTransactionHistory() {
        val userId = auth.currentUser?.uid ?: return
        
        showLoading(true)
        firebaseHelper.getTransactionHistory(userId) { transactions ->
            showLoading(false)
            if (transactions.isNotEmpty()) {
                showEmptyState(false)
                transactionAdapter.updateTransactions(transactions)
            } else {
                showEmptyState(true)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewHistory.visibility = if (show) View.GONE else View.VISIBLE
    }
}
