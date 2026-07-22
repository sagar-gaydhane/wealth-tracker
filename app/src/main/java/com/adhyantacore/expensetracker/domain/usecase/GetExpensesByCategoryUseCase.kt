package com.adhyantacore.expensetracker.domain.usecase

import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetExpensesByCategoryUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(category: String?): Flow<List<Expense>> {
        return if (category.isNullOrBlank() || category == ALL_CATEGORIES) {
            repository.getAllExpenses()
        } else {
            repository.getExpensesByCategory(category)
        }
    }

    companion object {
        const val ALL_CATEGORIES = "All"
    }
}