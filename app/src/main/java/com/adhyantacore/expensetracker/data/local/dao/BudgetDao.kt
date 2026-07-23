package com.adhyantacore.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adhyantacore.expensetracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE id = 1 LIMIT 1")
    fun getBudget(): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = 1")
    suspend fun deleteBudget()
}
