package com.adhyantacore.expensetracker.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
object DateFormatter {

    fun sectionLabel(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        val formatted = sdf.format(cal.time).uppercase()

        return when {
            isSameDay(cal, today) -> "TODAY, $formatted"
            isSameDay(cal, yesterday) -> "YESTERDAY, $formatted"
            else -> formatted
        }
    }

    fun dayKey(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}