package com.adhyantacore.expensetracker.domain.usecase

import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import jakarta.inject.Inject

// domain/usecase/AddExpenseUseCase.kt
class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Long> {
        return try {
            require(expense.amount > 0) { "Amount must be greater than zero" }
            require(expense.category.isNotBlank()) { "Category must be selected" }
            Result.success(repository.addExpense(expense))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

