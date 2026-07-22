package com.adhyantacore.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.model.ReceiptType


// data/local/ExpenseEntity.kt
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: String,
    val account: String,
    val notes: String?,
    val receiptUri: String?,
    val receiptType: String? ,
)

//fun ExpenseEntity.toDomain() = Expense(id, amount, category, date, account, notes, receiptUri, receiptType)
//
//fun Expense.toEntity() = ExpenseEntity(id, amount, category, date, account, notes, receiptUri, receiptType)