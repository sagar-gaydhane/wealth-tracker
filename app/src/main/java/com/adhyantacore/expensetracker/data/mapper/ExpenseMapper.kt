package com.adhyantacore.expensetracker.data.mapper

import com.adhyantacore.expensetracker.data.local.entity.ExpenseEntity
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.model.ReceiptType

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    amount = amount,
    category = category,
    date = date,
    account = account,
    notes = notes,
    receiptUri = receiptUri,
    receiptType = receiptType?.let { ReceiptType.valueOf(it) }
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    amount = amount,
    category = category,
    date = date,
    account = account,
    notes = notes,
    receiptUri = receiptUri,
    receiptType = receiptType?.name
)