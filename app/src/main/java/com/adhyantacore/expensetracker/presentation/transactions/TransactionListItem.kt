package com.adhyantacore.expensetracker.presentation.transactions

import com.adhyantacore.expensetracker.domain.model.Expense


sealed class TransactionListItem {
    data class DateHeader(val label: String, val dayKey: String) : TransactionListItem()
    data class Row(val expense: Expense) : TransactionListItem()
}