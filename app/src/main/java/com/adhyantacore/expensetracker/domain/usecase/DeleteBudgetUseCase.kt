package com.adhyantacore.expensetracker.domain.usecase

import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import jakarta.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke() = repository.deleteBudget()
}
