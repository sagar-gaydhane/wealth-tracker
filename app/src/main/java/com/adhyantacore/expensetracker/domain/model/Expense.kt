package com.adhyantacore.expensetracker.domain.model


data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: String,          // epoch millis, always store raw time, format at UI layer
    val account: String,
    val notes: String?,
    val receiptUri: String?,
    val receiptType: ReceiptType? , // "IMAGE" / "PDF" / null
//    val receiptType: String?  // "IMAGE" / "PDF" / null
)

enum class ReceiptType { IMAGE, PDF }

