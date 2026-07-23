package com.adhyantacore.expensetracker.domain.usecase

import com.adhyantacore.expensetracker.domain.repository.ExpenseRepository
import jakarta.inject.Inject

class SetBudgetUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(amount: Double) = repository.setBudget(amount)
}
