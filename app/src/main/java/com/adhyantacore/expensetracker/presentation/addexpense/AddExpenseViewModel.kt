package com.adhyantacore.expensetracker.presentation.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.model.ReceiptType
import com.adhyantacore.expensetracker.domain.usecase.AddExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _saveResult = MutableSharedFlow<Result<Long>>()
    val saveResult: SharedFlow<Result<Long>> = _saveResult

    fun saveExpense(
        amountText: String,
        category: String,
        date: String,
        account: String,
        notes: String,
        receiptUri: String?,
        receiptType: ReceiptType?
    ) {
        viewModelScope.launch {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            val result = addExpenseUseCase(
                Expense(
                    amount = amount,
                    category = category,
                    date = date,
                    account = account,
                    notes = notes.ifBlank { null },
                    receiptUri = receiptUri,
                    receiptType = receiptType
                )
            )
            _saveResult.emit(result)
        }
    }
}