package com.adhyantacore.expensetracker.presentation.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adhyantacore.expensetracker.databinding.ItemTransactionBinding
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.utils.CategoryUIHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentTransactionsAdapter(
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, RecentTransactionsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val style = CategoryUIHelper.getStyleForCategory(expense.category)
            val context = binding.root.context

            // Icon background & source
            binding.iconContainer.setBackgroundResource(style.bgDrawableRes)
            binding.ivCategoryIcon.setImageResource(style.iconRes)
            binding.ivCategoryIcon.setColorFilter(
                ContextCompat.getColor(context, style.tintColorRes)
            )

            // Merchant / Notes / Category Name
            val cleanCategory = expense.category.substringAfter(" ").trim()
            binding.tvMerchantName.text = expense.notes?.takeIf { it.isNotBlank() } ?: cleanCategory

            // Category tag
            binding.tvCategoryTag.text = cleanCategory

            // Format date time
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            binding.tvDateTime.text = sdf.format(Date(expense.date))

            // Amount
            binding.tvAmount.text = "-$" + String.format(Locale.getDefault(), "%.2f", expense.amount)

            binding.root.setOnClickListener { onItemClick(expense) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
