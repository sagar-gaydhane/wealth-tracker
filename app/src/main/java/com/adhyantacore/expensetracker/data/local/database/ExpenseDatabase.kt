package com.adhyantacore.expensetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adhyantacore.expensetracker.data.local.dao.ExpenseDao
import com.adhyantacore.expensetracker.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}

