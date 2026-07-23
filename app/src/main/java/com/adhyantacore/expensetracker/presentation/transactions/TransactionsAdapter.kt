package com.adhyantacore.expensetracker.presentation.transactions


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adhyantacore.expensetracker.databinding.ItemDateHeaderBinding
import com.adhyantacore.expensetracker.databinding.ItemTransactionRowBinding
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.presentation.transactiondetails.TransactionDetail
import java.util.Locale

class TransactionsAdapter(
    private val onItemClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit = {}
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is TransactionListItem.DateHeader -> TYPE_HEADER
            is TransactionListItem.Row -> TYPE_ROW
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
        } else {
            RowViewHolder(ItemTransactionRowBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.DateHeader -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.Row -> (holder as RowViewHolder).bind(
                item.expense,
                onItemClick,
                onDeleteClick
            )
        }
    }

    class HeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: TransactionListItem.DateHeader) {
            binding.tvDateHeader.text = header.label
        }
    }

    class RowViewHolder(private val binding: ItemTransactionRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            expense: Expense,
            onItemClick: (Expense) -> Unit,
            onDeleteClick: (Expense) -> Unit
        ) {
            val emoji = expense.category.substringBefore(" ", missingDelimiterValue = "📦")
            val name =
                expense.category.substringAfter(" ", missingDelimiterValue = expense.category)

            binding.tvCategoryEmoji.text = emoji
            binding.tvTitle.text = name
            binding.tvSubtitle.text = listOfNotNull(
                expense.account,
                expense.notes?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            binding.tvAmount.text =
                "-$" + String.format(Locale.getDefault(), "%.2f", expense.amount)

            // Delete button click handler (don't propagate to root)
            binding.btnDelete.setOnClickListener {
                onDeleteClick(expense)
            }

            // Root item click handler (navigate to detail)
            binding.main.setOnClickListener {
                val intent = Intent(binding.root.context, TransactionDetail::class.java).apply {
                    putExtra("expense", expense)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                binding.root.context.startActivity(intent)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
        override fun areItemsTheSame(old: TransactionListItem, new: TransactionListItem): Boolean {
            return when {
                old is TransactionListItem.DateHeader && new is TransactionListItem.DateHeader ->
                    old.dayKey == new.dayKey

                old is TransactionListItem.Row && new is TransactionListItem.Row ->
                    old.expense.id == new.expense.id

                else -> false
            }
        }

        override fun areContentsTheSame(
            old: TransactionListItem,
            new: TransactionListItem
        ): Boolean =
            old == new
    }
}