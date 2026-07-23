package com.adhyantacore.expensetracker.domain.usecase

import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetBudgetUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<Double?> = repository.getBudget()
}
