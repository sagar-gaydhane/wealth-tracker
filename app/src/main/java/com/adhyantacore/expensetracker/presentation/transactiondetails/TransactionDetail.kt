package com.adhyantacore.expensetracker.presentation.transactiondetails

import android.content.Intent
import android.Manifest
import android.widget.Toast
import androidx.core.content.FileProvider
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adhyantacore.expensetracker.R
import com.adhyantacore.expensetracker.databinding.ActivityTransactionDetailsBinding
import com.adhyantacore.expensetracker.domain.model.Expense
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.adhyantacore.expensetracker.domain.usecase.DeleteExpenseUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionDetail : AppCompatActivity() {

    companion object {
        private const val REQUEST_READ_STORAGE = 101
    }

    @Inject
    lateinit var deleteExpenseUseCase: DeleteExpenseUseCase

    private lateinit var binding: ActivityTransactionDetailsBinding
    private var expense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get expense from intent
        expense = intent.getParcelableExtra("expense") as? Expense

        if (expense != null) {
            displayExpenseDetails(expense!!)
        }

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Delete button
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        // Edit button
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditTransactionsActivity::class.java).apply {
                putExtra("expense", expense)
            }
            startActivity(intent)
        }

        // Share button
        binding.btnShare.setOnClickListener {
            expense?.let { shareExpense(it) }
        }

        // Zoom receipt button
        binding.btnZoomReceipt.setOnClickListener {
            expense?.receiptUri?.let { uriStr ->
                zoomReceipt(uriStr)
            }
        }
    }

    private fun displayExpenseDetails(expense: Expense) {
        // Category emoji and name
        val emoji = expense.category.substringBefore(" ", missingDelimiterValue = "📦")
        val categoryName =
            expense.category.substringAfter(" ", missingDelimiterValue = expense.category)
        binding.tvCategoryLabel.text = categoryName

        // Amount
        binding.tvAmount.text = "-$" + String.format(Locale.getDefault(), "%.2f", expense.amount)

        // Notes (merchant/title - using category name as placeholder)
        binding.tvMerchant.text = expense.notes ?: categoryName

        // Date formatting
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        val formattedDate = sdf.format(Date(expense.date))
        binding.tvDateValue.text = formattedDate

        // Account (Paid with)
        binding.tvPaidWithValue.text = expense.account

        // Notes display
        if (expense.notes != null && expense.notes.isNotBlank()) {
            binding.tvNotes.text = "\"${expense.notes}\""
        } else {
            binding.tvNotes.text = "No notes added"
        }

        // Receipt URI (if available)
        if (expense.receiptUri != null && expense.receiptUri.isNotBlank()) {
            loadReceiptImage(expense.receiptUri)
        } else {
            // No receipt, set placeholder background
            binding.ivReceipt.setImageResource(R.drawable.bg_receipt_placeholder)
        }
    }

    private fun loadReceiptImage(uriString: String) {
        Picasso.get().load(android.net.Uri.parse(uriString))
            .placeholder(R.drawable.bg_receipt_placeholder).error(R.drawable.bg_receipt_placeholder)
            .fit().centerInside().into(binding.ivReceipt)
    }

    private fun zoomReceipt(uriString: String) {
        try {
            val uri = android.net.Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW)
            
            val finalUri = if (uri.scheme == "file") {
                val file = java.io.File(uri.path!!)
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            } else {
                uri
            }
            
            intent.setDataAndType(finalUri, "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to view receipt", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareExpense(expense: Expense) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = sdf.format(Date(expense.date))
        val amountString = String.format(Locale.getDefault(), "%.2f", expense.amount)
        
        val categoryName = expense.category.substringAfter(" ", missingDelimiterValue = expense.category)
        
        val shareText = """
            Here are the details for a recent expense:
            
            I spent $$amountString on $categoryName on $dateString.
            This was paid using my ${expense.account}.
            
            Notes: ${if (expense.notes.isNullOrBlank()) "None" else expense.notes}
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Expense Details: $categoryName")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Expense via"))
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this).setTitle("Delete Transaction?")
            .setMessage("Are you sure you want to delete this transaction?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("Delete") { dialog, _ ->
                expense?.let {
                    lifecycleScope.launch {
                        deleteExpenseUseCase(it)
                        dialog.dismiss()
                        finish()
                    }
                } ?: run {
                    dialog.dismiss()
                    finish()
                }
            }.show()
    }
}