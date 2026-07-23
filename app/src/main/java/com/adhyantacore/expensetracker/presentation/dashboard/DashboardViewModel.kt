package com.adhyantacore.expensetracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.usecase.GetExpensesUseCase
import com.adhyantacore.expensetracker.domain.usecase.GetBudgetUseCase
import com.adhyantacore.expensetracker.domain.usecase.SetBudgetUseCase
import com.adhyantacore.expensetracker.domain.usecase.DeleteBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val totalBalance: Double = 12450.80,
    val income: Double = 5240.00,
    val spend: Double = 3120.00,
    val savings: Double = 1200.00,
    val budgetLimit: Double? = null,
    val budgetLeft: Double = 450.00,
    val budgetProgress: Int = 30,
    val recentTransactions: List<Expense> = emptyList(),
    val weeklySpends: List<Double> = List(7) { 0.0 },
    val avgSpendPerDay: Double = 0.0,
    val percentChangeText: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getBudgetUseCase: GetBudgetUseCase,
    private val setBudgetUseCase: SetBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        getExpensesUseCase(),
        getBudgetUseCase()
    ) { expenses, budget ->
        val startingTotalBalance = 15000.00
        val startingSavings = 2000.00
        val mockIncome = 5240.00

        val totalSpend = expenses.sumOf { it.amount }
        val currentTotalBalance = maxOf(0.0, startingTotalBalance - totalSpend)
        
        val savingsSpend = expenses.filter { it.account.equals("Saving", ignoreCase = true) }.sumOf { it.amount }
        val currentSavings = maxOf(0.0, startingSavings - savingsSpend)
        
        val currentBudgetLeft = if (budget != null) maxOf(0.0, budget - totalSpend) else 0.0
        val progressPercent = if (budget != null && budget > 0) {
            ((currentBudgetLeft / budget) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val recents = expenses.take(5)

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else 2 - currentDayOfWeek
        
        val currentWeekStartCal = cal.clone() as Calendar
        currentWeekStartCal.add(Calendar.DAY_OF_YEAR, daysToMonday)
        val currentWeekStart = currentWeekStartCal.timeInMillis

        val currentWeekEndCal = currentWeekStartCal.clone() as Calendar
        currentWeekEndCal.add(Calendar.DAY_OF_YEAR, 7)
        val currentWeekEnd = currentWeekEndCal.timeInMillis

        val prevWeekStartCal = currentWeekStartCal.clone() as Calendar
        prevWeekStartCal.add(Calendar.DAY_OF_YEAR, -7)
        val prevWeekStart = prevWeekStartCal.timeInMillis
        val prevWeekEnd = currentWeekStart

        val weeklySpends = MutableList(7) { 0.0 }
        var currentWeekTotal = 0.0

        for (expense in expenses) {
            if (expense.date in currentWeekStart until currentWeekEnd) {
                val expCal = Calendar.getInstance().apply { timeInMillis = expense.date }
                val dayOfWeek = expCal.get(Calendar.DAY_OF_WEEK)
                val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                if (index in 0..6) {
                    weeklySpends[index] += expense.amount
                }
                currentWeekTotal += expense.amount
            }
        }

        var prevWeekTotal = 0.0
        for (expense in expenses) {
            if (expense.date in prevWeekStart until prevWeekEnd) {
                prevWeekTotal += expense.amount
            }
        }

        val percentChangeText = if (prevWeekTotal > 0) {
            if (currentWeekTotal > prevWeekTotal) {
                val pct = ((currentWeekTotal - prevWeekTotal) / prevWeekTotal) * 100
                "↗ ${pct.toInt()}% more"
            } else if (currentWeekTotal < prevWeekTotal) {
                val pct = ((prevWeekTotal - currentWeekTotal) / prevWeekTotal) * 100
                "↘ ${pct.toInt()}% less"
            } else {
                "0% change"
            }
        } else {
            "—"
        }

        val avgSpendPerDay = currentWeekTotal / 7.0

        DashboardUiState(
            totalBalance = currentTotalBalance,
            income = mockIncome,
            spend = totalSpend,
            savings = currentSavings,
            budgetLimit = budget,
            budgetLeft = currentBudgetLeft,
            budgetProgress = progressPercent,
            recentTransactions = recents,
            weeklySpends = weeklySpends,
            avgSpendPerDay = avgSpendPerDay,
            percentChangeText = percentChangeText,
            isLoading = false
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            setBudgetUseCase(amount)
        }
    }

    fun deleteBudget() {
        viewModelScope.launch {
            deleteBudgetUseCase()
        }
    }
}
