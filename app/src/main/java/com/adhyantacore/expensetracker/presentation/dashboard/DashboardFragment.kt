package com.adhyantacore.expensetracker.presentation.dashboard

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adhyantacore.expensetracker.R
import com.adhyantacore.expensetracker.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.Intent
import android.text.InputType
import android.widget.EditText
import com.adhyantacore.expensetracker.presentation.transactiondetails.TransactionDetail
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var recentAdapter: RecentTransactionsAdapter
private var currentBudget: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        recentAdapter = RecentTransactionsAdapter(
            onItemClick = { expense ->
                val intent = Intent(requireContext(), TransactionDetail::class.java).apply {
                    putExtra("expense", expense)
                }
                startActivity(intent)
            }
        )
        binding.rvRecentTransactions.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.transactionFragment)
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.addExpenseFragment)
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }
        binding.cardBudget.setOnClickListener {
            showBudgetDialog(currentBudget)
        }
    }

    private fun showBudgetDialog(current: Double?) {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(current?.toString() ?: "")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (current != null) "Edit Budget" else "Set Budget")
            .setView(editText)
            .setPositiveButton(if (current != null) "Update" else "Set") { _, _ ->
                val newBudget = editText.text.toString().toDoubleOrNull()
                if (newBudget != null) {
                    viewModel.setBudget(newBudget)
                }
            }
            .setNegativeButton("Cancel", null)
            .apply {
                if (current != null) {
                    setNeutralButton("Delete") { _, _ ->
                        viewModel.deleteBudget()
                    }
                }
            }
            .show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateMetrics(state)
currentBudget = state.budgetLimit
                    updateWeeklyChart(state)
                    recentAdapter.submitList(state.recentTransactions)
                }
            }
        }
    }

    private fun updateMetrics(state: DashboardUiState) {
        binding.tvTotalBalance.text = "$" + String.format(Locale.getDefault(), "%,.2f", state.totalBalance)
        binding.tvIncome.text = "$" + String.format(Locale.getDefault(), "%,.2f", state.income)
        binding.tvSpend.text = "$" + String.format(Locale.getDefault(), "%,.2f", state.spend)
        binding.tvSavings.text = "$" + String.format(Locale.getDefault(), "%,.2f", state.savings)
        binding.tvBudgetLeft.text = "$" + String.format(Locale.getDefault(), "%,.2f", state.budgetLeft)

        // Progress bar
        val progressParams = binding.vBudgetProgress.layoutParams as LinearLayout.LayoutParams
        progressParams.weight = state.budgetProgress.toFloat()
        binding.vBudgetProgress.layoutParams = progressParams
    }

    private fun updateWeeklyChart(state: DashboardUiState) {
        // Average spend per day
        binding.tvAvgSpend.text = "Avg. $" + String.format(Locale.getDefault(), "%,.0f", state.avgSpendPerDay) + " / day"

        // Percent change text and color
        binding.tvPercentChange.text = state.percentChangeText
        if (state.percentChangeText.contains("less")) {
            binding.tvPercentChange.setTextColor(resources.getColor(R.color.green_positive, null))
        } else if (state.percentChangeText.contains("more")) {
            binding.tvPercentChange.setTextColor(resources.getColor(R.color.red_negative, null))
        } else {
            binding.tvPercentChange.setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        // Highlight current day and set bar heights
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val currentDayIndex = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2

        val bars = listOf(
            binding.weeklyChart.barMon,
            binding.weeklyChart.barTue,
            binding.weeklyChart.barWed,
            binding.weeklyChart.barThu,
            binding.weeklyChart.barFri,
            binding.weeklyChart.barSat,
            binding.weeklyChart.barSun
        )

        val maxSpend = state.weeklySpends.maxOrNull() ?: 0.0

        for (i in 0 until 7) {
            val bar = bars[i]
            val spend = state.weeklySpends[i]

            // Highlight
            if (i == currentDayIndex) {
                bar.setBackgroundResource(R.drawable.bg_chart_bar_active)
            } else {
                bar.setBackgroundResource(R.drawable.bg_chart_bar_default)
            }

            // Height scaling (max design height is 80dp)
            val heightDp = if (maxSpend > 0) {
                (spend / maxSpend) * 80.0
            } else {
                0.0
            }
            val finalHeightDp = if (spend > 0) maxOf(heightDp, 4.0) else 0.0

            val params = bar.layoutParams
            params.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                finalHeightDp.toFloat(),
                resources.displayMetrics
            ).toInt()
            bar.layoutParams = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
