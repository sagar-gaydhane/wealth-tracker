package com.adhyantacore.expensetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adhyantacore.expensetracker.data.local.dao.ExpenseDao
import com.adhyantacore.expensetracker.data.local.dao.BudgetDao
import com.adhyantacore.expensetracker.data.local.entity.ExpenseEntity
import com.adhyantacore.expensetracker.data.local.entity.BudgetEntity

@Database(entities = [ExpenseEntity::class, BudgetEntity::class], version = 3, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
}

