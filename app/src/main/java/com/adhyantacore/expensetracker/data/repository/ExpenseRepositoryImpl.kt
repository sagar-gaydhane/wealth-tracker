package com.adhyantacore.expensetracker.data.repository

import com.adhyantacore.expensetracker.data.local.dao.ExpenseDao
import com.adhyantacore.expensetracker.data.local.dao.BudgetDao
import com.adhyantacore.expensetracker.data.local.entity.BudgetEntity
import com.adhyantacore.expensetracker.data.mapper.toDomain
import com.adhyantacore.expensetracker.data.mapper.toEntity
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao,
    private val budgetDao: BudgetDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    override fun getExpensesByCategory(category: String): Flow<List<Expense>> =
        dao.getExpensesByCategory(category).map { list -> list.map { it.toDomain() } }

    override suspend fun addExpense(expense: Expense): Long =
        dao.insertExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense.toEntity())

    override fun getAllCategories(): Flow<List<String>> =
        dao.getAllCategories()

    override fun getBudget(): Flow<Double?> =
        budgetDao.getBudget().map { it?.amount }

    override suspend fun setBudget(amount: Double) =
        budgetDao.insertBudget(BudgetEntity(amount = amount))

    override suspend fun deleteBudget() =
        budgetDao.deleteBudget()
}


