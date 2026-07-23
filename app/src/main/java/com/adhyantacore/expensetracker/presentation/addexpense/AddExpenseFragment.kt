package com.adhyantacore.expensetracker.presentation.addexpense

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.adhyantacore.expensetracker.AccountType
import com.adhyantacore.expensetracker.R
import com.adhyantacore.expensetracker.databinding.FragmentAddExpensBinding
import com.adhyantacore.expensetracker.domain.model.ReceiptType
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpensBinding
    private val viewModel: AddExpenseViewModel by viewModels()
    private var amount = 0.0
    private val categories = arrayOf(
        "🍔 Food",
        "🚕 Transport",
        "🛒 Shopping",
        "🎬 Entertainment",
        "💡 Bills",
        "🏥 Health",
        "📚 Education",
        "✈️ Travel",
        "🏠 Rent",
        "💼 Business",
        "🎁 Gift",
        "📱 Mobile Recharge",
        "💻 Electronics",
        "🐶 Pets",
        "📦 Others"
    )
    private var selectedCategory = ""
    private var selectedAccount = "Main Wallet"

    // ---- Receipt state ----
    private var receiptUri: Uri? = null
    private var receiptType: ReceiptType? = null
    private var cameraPhotoUri: Uri? = null

    private var selectedDateMillis: Long = System.currentTimeMillis()

    // ---- Activity result launchers ----

    // Camera capture -> full-size photo saved to a FileProvider uri
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraPhotoUri != null) {
                setReceipt(cameraPhotoUri!!, ReceiptType.IMAGE)
            }
        }

    // Camera permission request
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to take a photo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun copyUriToInternalStorage(uri: Uri, isPdf: Boolean = false): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val dir = java.io.File(requireContext().cacheDir, "receipts").apply { mkdirs() }
            val ext = if (isPdf) ".pdf" else ".jpg"
            val fileName = "receipt_${System.currentTimeMillis()}$ext"
            val file = java.io.File(dir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Gallery image picker (Android 13+ Photo Picker, falls back automatically on older versions)
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val localUri = copyUriToInternalStorage(uri, false)
            if (localUri != null) {
                setReceipt(localUri, ReceiptType.IMAGE)
            } else {
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // PDF picker
    private val pickPdfLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val localUri = copyUriToInternalStorage(uri, true)
            if (localUri != null) {
                setReceipt(localUri, ReceiptType.PDF)
            } else {
                Toast.makeText(requireContext(), "Failed to process PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddExpensBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etAmount.setText("0")

        binding.btnIncrease.setOnClickListener {
            amount = getAmount() + 1
            binding.etAmount.setText(formatAmount(amount))
            binding.etAmount.setSelection(binding.etAmount.text.length)
        }

        binding.btnDecrease.setOnClickListener {
            amount = getAmount()

            if (amount > 0) {
                amount -= 1
                binding.etAmount.setText(formatAmount(amount))
                binding.etAmount.setSelection(binding.etAmount.text.length)
            }
        }

        binding.fieldCategory.setOnClickListener {
            showCategoryDialog()
        }

        // Show today's date initially
        binding.tvDateValue.text = getCurrentDate()

        binding.fieldDate.setOnClickListener {
            showDatePicker()
        }

        binding.pillMainWallet.setOnClickListener {
            selectAccount(AccountType.MAIN_WALLET)
        }

        binding.pillCreditCard.setOnClickListener {
            selectAccount(AccountType.CREDIT_CARD)
        }

        binding.pillSaving.setOnClickListener {
            selectAccount(AccountType.SAVING)
        }
        selectAccount(AccountType.MAIN_WALLET)

        // ---- Receipt upload ----
        binding.btnAddReceipt.setOnClickListener { showReceiptSourceDialog() }
        binding.btnRemoveReceipt.setOnClickListener { clearReceipt() }


        binding.btnSave.setOnClickListener {

            val amountText = binding.etAmount.text.toString()
            val category = selectedCategory
            val account = selectedAccount
            val notes = binding.etNotes.text.toString()

            if (category.isBlank()) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateString = sdf.format(Date(selectedDateMillis))

            viewModel.saveExpense(
                amountText = amountText,
                category = category,
                date = dateString,
                account = account,
                notes = notes,
                receiptUri = receiptUri?.toString(),
                receiptType = receiptType    // no .name conversion here anymore
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Expense saved", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }.onFailure {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun getAmount(): Double {
        return binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun formatAmount(value: Double): String {
        return if (value % 1 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    private fun showCategoryDialog() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Category")
            .setItems(categories) { _, which ->

                selectedCategory = categories[which]

                binding.tvCategoryValue.text = selectedCategory
                binding.tvCategoryValue.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                )
            }
            .show()
    }

    private fun showDatePicker() {

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.show(parentFragmentManager, "DATE_PICKER")

        picker.addOnPositiveButtonClickListener { selection ->

            selectedDateMillis = selection

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val selectedDate = sdf.format(Date(selection))

            binding.tvDateValue.text = selectedDate
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun selectAccount(type: AccountType) {

        resetAccountViews()

        when (type) {

            AccountType.MAIN_WALLET -> {

                selectedAccount = "Main Wallet"

                binding.pillMainWallet.setBackgroundResource(R.drawable.bg_account_pill_selected)

                binding.pillMainWalletIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )

                binding.tvMainWallet.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
            }

            AccountType.CREDIT_CARD -> {

                selectedAccount = "Credit Card"

                binding.pillCreditCard.setBackgroundResource(R.drawable.bg_account_pill_selected)

                binding.pillCreditCardIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )

                binding.tvCreditCard.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
            }

            AccountType.SAVING -> {

                selectedAccount = "Saving"

                binding.pillSaving.setBackgroundResource(R.drawable.bg_account_pill_selected)

                binding.pillSavingIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )

                binding.tvSaving.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
            }
        }
    }

    private fun resetAccountViews() {

        // Backgrounds
        binding.pillMainWallet.setBackgroundResource(R.drawable.bg_account_pill_unselected)
        binding.pillCreditCard.setBackgroundResource(R.drawable.bg_account_pill_unselected)
        binding.pillSaving.setBackgroundResource(R.drawable.bg_account_pill_unselected)

        // Icons
        val gray = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        binding.pillMainWalletIcon.setColorFilter(gray)
        binding.pillCreditCardIcon.setColorFilter(gray)
        binding.pillSavingIcon.setColorFilter(gray)

        // Text
        binding.tvMainWallet.setTextColor(gray)
        binding.tvCreditCard.setTextColor(gray)
        binding.tvSaving.setTextColor(gray)
    }


    // ---------------- Receipt picking ----------------

    private fun showReceiptSourceDialog() {
        val options = arrayOf("Take Photo", "Choose Image", "Choose PDF")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                    2 -> pickPdfLauncher.launch("application/pdf")
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        val authority = "${requireContext().packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(requireContext(), authority, photoFile)
        cameraPhotoUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun createImageFile(): File {
        val dir = File(requireContext().cacheDir, "receipts").apply { mkdirs() }
        val fileName = "receipt_${System.currentTimeMillis()}.jpg"
        return File(dir, fileName)
    }

    private fun setReceipt(uri: Uri, type: ReceiptType) {
        receiptUri = uri
        receiptType = type

        binding.layoutReceiptPreview.visibility = View.VISIBLE
        binding.tvReceiptFileName.text = getDisplayName(uri, type)

        when (type) {
            ReceiptType.IMAGE -> {
                com.squareup.picasso.Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(binding.ivReceiptThumb)
            }
            ReceiptType.PDF -> binding.ivReceiptThumb.setImageResource(R.drawable.ic_dashboard) // swap for a PDF icon
        }

        binding.tvReceiptLabel.text = "Change receipt"
    }

    private fun clearReceipt() {
        receiptUri = null
        receiptType = null
        binding.layoutReceiptPreview.visibility = View.GONE
        binding.tvReceiptLabel.text = getString(R.string.add_receipt_image)
    }

    private fun getDisplayName(uri: Uri, type: ReceiptType): String {
        if (type == ReceiptType.IMAGE && uri == cameraPhotoUri) {
            return uri.lastPathSegment ?: "receipt.jpg"
        }
        var name: String? = null
        try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            // Ignore for file:// URIs
        }
        return name ?: uri.lastPathSegment ?: "receipt"
    }
}