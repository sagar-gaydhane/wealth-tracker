package com.adhyantacore.expensetracker.presentation.transactions

// presentation/transactions/TransactionsUiState.kt
data class TransactionsUiState(
    val items: List<TransactionListItem> = emptyList(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)