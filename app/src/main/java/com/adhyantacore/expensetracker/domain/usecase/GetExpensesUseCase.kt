package com.adhyantacore.expensetracker.domain.usecase

import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

// domain/usecase/GetExpensesUseCase.kt
class GetExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> = repository.getAllExpenses()
}