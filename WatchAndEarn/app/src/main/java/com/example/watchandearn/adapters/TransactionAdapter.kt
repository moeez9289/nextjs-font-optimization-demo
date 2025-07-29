package com.example.watchandearn.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watchandearn.databinding.ItemTransactionBinding
import com.example.watchandearn.utils.Constants
import com.example.watchandearn.utils.FirebaseHelper

class TransactionAdapter(
    private val firebaseHelper: FirebaseHelper
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    
    private var transactions = mutableListOf<Map<String, Any>>()
    
    fun updateTransactions(newTransactions: List<Map<String, Any>>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }
    
    override fun getItemCount(): Int = transactions.size
    
    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(transaction: Map<String, Any>) {
            val type = transaction["type"] as? String ?: ""
            val amount = (transaction["amount"] as? Long)?.toInt() ?: 0
            val description = transaction["description"] as? String ?: ""
            val timestamp = transaction["timestamp"] as? Long ?: 0L
            
            // Set transaction type
            binding.tvTransactionType.text = getTransactionTypeDisplay(type)
            
            // Set description
            binding.tvDescription.text = description
            
            // Set date
            binding.tvDate.text = firebaseHelper.formatTimestamp(timestamp)
            
            // Set amount with proper sign and color
            val amountText = if (amount >= 0) "+$amount" else "$amount"
            binding.tvAmount.text = amountText
            
            // Set amount color based on positive/negative
            val amountColor = if (amount >= 0) {
                binding.root.context.getColor(android.R.color.holo_green_light)
            } else {
                binding.root.context.getColor(android.R.color.holo_red_light)
            }
            binding.tvAmount.setTextColor(amountColor)
        }
        
        private fun getTransactionTypeDisplay(type: String): String {
            return when (type) {
                Constants.TRANSACTION_AD_WATCH -> "Ad Watch"
                Constants.TRANSACTION_DAILY_BONUS -> "Daily Bonus"
                Constants.TRANSACTION_REFERRAL_BONUS -> "Referral Bonus"
                Constants.TRANSACTION_WITHDRAWAL -> "Withdrawal"
                else -> "Transaction"
            }
        }
    }
}
