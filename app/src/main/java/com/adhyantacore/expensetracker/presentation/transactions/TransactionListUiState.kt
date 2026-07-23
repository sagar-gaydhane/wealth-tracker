package com.adhyantacore.expensetracker.presentation.transactions


import com.adhyantacore.expensetracker.domain.model.Expense

sealed class TransactionListUiState {
    object Loading : TransactionListUiState()
    data class Success(val expenses: List<Expense>) : TransactionListUiState()
    object Empty : TransactionListUiState()
    data class Error(val message: String) : TransactionListUiState()
}