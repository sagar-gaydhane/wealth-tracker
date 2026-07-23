package com.adhyantacore.expensetracker.presentation.transactions

    import android.content.Intent
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.core.content.ContextCompat
    import androidx.core.view.isVisible
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.Lifecycle
    import androidx.lifecycle.lifecycleScope
    import androidx.lifecycle.repeatOnLifecycle
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.adhyantacore.expensetracker.R
    import com.adhyantacore.expensetracker.databinding.FragmentTransactionsBinding
    import com.adhyantacore.expensetracker.presentation.transactiondetails.TransactionDetail
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import dagger.hilt.android.AndroidEntryPoint
    import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment() {

    private lateinit var binding: FragmentTransactionsBinding
    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionsAdapter(
            onItemClick = { expense ->
                // Navigate to transaction detail screen
                val intent = Intent(requireContext(), TransactionDetail::class.java)
                intent.putExtra("expense", expense)
                startActivity(intent)
            },
            onDeleteClick = { expense ->
                showDeleteConfirmation(expense)
            }
        )
        binding.rvTransactions.adapter = adapter
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        setupChipListeners()



        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.items)
                    binding.rvTransactions.isVisible = !state.isEmpty
                    binding.tvEmptyState.isVisible = state.isEmpty && !state.isLoading
                    updateChipSelection(state.selectedFilter)
                }
            }
        }
    }

    private fun setupChipListeners() {
        val chipMap = mapOf(
            binding.chipAll to TransactionFilter.ALL,
            binding.chipFood to TransactionFilter.FOOD,
            binding.chipRent to TransactionFilter.RENT,
            binding.chipTravel to TransactionFilter.TRAVEL,
            binding.chipTransport to TransactionFilter.TRANSPORT,
            binding.chipShopping to TransactionFilter.SHOPPING,
            binding.chipEntertainment to TransactionFilter.ENTERTAINMENT,
            binding.chipBills to TransactionFilter.BILLS,
            binding.chipHealth to TransactionFilter.HEALTH,
            binding.chipEducation to TransactionFilter.EDUCATION,
            binding.chipBusiness to TransactionFilter.BUSINESS,
            binding.chipGift to TransactionFilter.GIFT,
            binding.chipMobileRecharge to TransactionFilter.MOBILE_RECHARGE,
            binding.chipElectronics to TransactionFilter.ELECTRONICS,
            binding.chipPets to TransactionFilter.PETS,
            binding.chipOthers to TransactionFilter.OTHERS
        )
        chipMap.forEach { (chip, filter) ->
            chip.setOnClickListener { viewModel.onFilterSelected(filter) }
        }
    }

    private fun updateChipSelection(selected: TransactionFilter) {
        val chipMap = mapOf(
            binding.chipAll to TransactionFilter.ALL,
            binding.chipFood to TransactionFilter.FOOD,
            binding.chipRent to TransactionFilter.RENT,
            binding.chipTravel to TransactionFilter.TRAVEL,
            binding.chipTransport to TransactionFilter.TRANSPORT,
            binding.chipShopping to TransactionFilter.SHOPPING,
            binding.chipEntertainment to TransactionFilter.ENTERTAINMENT,
            binding.chipBills to TransactionFilter.BILLS,
            binding.chipHealth to TransactionFilter.HEALTH,
            binding.chipEducation to TransactionFilter.EDUCATION,
            binding.chipBusiness to TransactionFilter.BUSINESS,
            binding.chipGift to TransactionFilter.GIFT,
            binding.chipMobileRecharge to TransactionFilter.MOBILE_RECHARGE,
            binding.chipElectronics to TransactionFilter.ELECTRONICS,
            binding.chipPets to TransactionFilter.PETS,
            binding.chipOthers to TransactionFilter.OTHERS
        )
        chipMap.forEach { (chip, filter) ->
            val isSelected = filter == selected
            chip.setBackgroundResource(
                if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
            )
            chip.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.chip_text_selected else R.color.chip_text_unselected
                )
            )
        }
    }

    private fun showDeleteConfirmation(expense: com.adhyantacore.expensetracker.domain.model.Expense) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Transaction?")
            .setMessage("Are you sure you want to delete this transaction?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteExpense(expense)
                dialog.dismiss()
            }
            .show()
    }
}