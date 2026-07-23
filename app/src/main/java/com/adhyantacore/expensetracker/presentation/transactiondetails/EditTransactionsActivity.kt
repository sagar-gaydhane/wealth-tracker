package com.adhyantacore.expensetracker.presentation.transactiondetails

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.adhyantacore.expensetracker.MainActivity
import com.adhyantacore.expensetracker.databinding.ActivityEditTransactionBinding
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.usecase.AddExpenseUseCase
import com.adhyantacore.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EditTransactionsActivity : AppCompatActivity() {

    @Inject
    lateinit var addExpenseUseCase: AddExpenseUseCase

    @Inject
    lateinit var deleteExpenseUseCase: DeleteExpenseUseCase

    private lateinit var binding: ActivityEditTransactionBinding
    private var expense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        expense = intent.getParcelableExtra("expense") as? Expense

        expense?.let {
            populateUI(it)
        } ?: run {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDeleteIcon.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnUpdate.setOnClickListener {
            updateExpense()
        }
    }

    private fun populateUI(expense: Expense) {
        binding.etAmount.setText(String.format(Locale.US, "%.2f", expense.amount))

        val categoryName =
            expense.category.substringAfter(" ", missingDelimiterValue = expense.category)
        binding.tvCategoryValue.text = categoryName

        binding.tvAccountValue.text = expense.account

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvDateValue.text = sdf.format(Date(expense.date))

        binding.etNotes.setText(expense.notes ?: "")

        if (expense.receiptUri.isNullOrEmpty()) {
            binding.rowReceipt.visibility = android.view.View.GONE
        } else {
            binding.rowReceipt.visibility = android.view.View.VISIBLE
            binding.btnViewReceipt.setOnClickListener {
                zoomReceipt(expense.receiptUri)
            }
            binding.rowReceipt.setOnClickListener {
                zoomReceipt(expense.receiptUri)
            }
        }
    }

    private fun zoomReceipt(imagePath: String) {
        try {
            val file = java.io.File(imagePath)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun updateExpense() {
        expense?.let { currentExpense ->
            val amountStr = binding.etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull()

            if (amount == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return
            }

            val notes = binding.etNotes.text.toString().trim().takeIf { it.isNotEmpty() }

            val updatedExpense = currentExpense.copy(
                amount = amount,
                notes = notes
            )

            lifecycleScope.launch {
                val result = addExpenseUseCase(updatedExpense)
                if (result.isSuccess) {
                    val intent =
                        Intent(this@EditTransactionsActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@EditTransactionsActivity,
                        "Failed to update",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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
                        val intent =
                            Intent(this@EditTransactionsActivity, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        startActivity(intent)
                        finish()
                    }
                } ?: dialog.dismiss()
            }.show()
    }
}