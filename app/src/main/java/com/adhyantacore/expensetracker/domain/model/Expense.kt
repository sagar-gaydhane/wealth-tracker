package com.adhyantacore.expensetracker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: Long,          // epoch millis, always store raw time, format at UI layer
    val account: String,
    val notes: String?,
    val receiptUri: String?,
    val receiptType: ReceiptType? , // "IMAGE" / "PDF" / null
) : Parcelable

@Parcelize
enum class ReceiptType : Parcelable { IMAGE, PDF }

