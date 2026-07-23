package com.adhyantacore.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: Int = 1,
    val amount: Double
)
