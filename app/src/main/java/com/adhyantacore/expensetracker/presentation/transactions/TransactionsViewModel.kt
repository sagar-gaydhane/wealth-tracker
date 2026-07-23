package com.adhyantacore.expensetracker.presentation.transactions


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adhyantacore.expensetracker.domain.model.Expense
import com.adhyantacore.expensetracker.domain.usecase.GetExpensesByCategoryUseCase
import com.adhyantacore.expensetracker.domain.usecase.GetExpensesByCategoryUseCase.Companion.ALL_CATEGORIES
import com.adhyantacore.expensetracker.domain.usecase.DeleteExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getExpensesByCategoryUseCase: GetExpensesByCategoryUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionsUiState> = _selectedFilter
        .flatMapLatest { filter ->
            getExpensesByCategoryUseCase(filter.category ?: ALL_CATEGORIES)
                .map { expenses ->
                    val items = expenses
                        .groupBy { expense ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            sdf.format(Date(expense.date))
                        }
                        .flatMap { (date, expenseList) ->
                            listOf(TransactionListItem.DateHeader(date, date)) +
                            expenseList.map { TransactionListItem.Row(it) }
                        }
                    TransactionsUiState(
                        items = items,
                        selectedFilter = filter,
                        isLoading = false,
                        isEmpty = items.isEmpty()
                    )
                }
                .catch {
                    emit(TransactionsUiState(
                        items = emptyList(),
                        selectedFilter = filter,
                        isLoading = false,
                        isEmpty = true
                    ))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsUiState())

    fun onFilterSelected(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
        }
    }
}