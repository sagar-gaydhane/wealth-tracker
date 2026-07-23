package com.adhyantacore.expensetracker.utils

import com.adhyantacore.expensetracker.R

data class CategoryStyle(
    val iconRes: Int,
    val bgDrawableRes: Int,
    val tintColorRes: Int
)

object CategoryUIHelper {
    fun getStyleForCategory(category: String): CategoryStyle {
        val cleanCategory = category.substringAfter(" ").trim().lowercase()
        return when {
            cleanCategory.contains("food") -> CategoryStyle(
                iconRes = R.drawable.ic_fork_knife,
                bgDrawableRes = R.drawable.bg_icon_circle_green,
                tintColorRes = R.color.green_positive
            )
            cleanCategory.contains("transport") || cleanCategory.contains("car") -> CategoryStyle(
                iconRes = R.drawable.ic_car,
                bgDrawableRes = R.drawable.bg_icon_circle_blue,
                tintColorRes = R.color.primary_blue
            )
            cleanCategory.contains("shopping") || cleanCategory.contains("cart") -> CategoryStyle(
                iconRes = R.drawable.ic_cart,
                bgDrawableRes = R.drawable.bg_icon_circle_pink,
                tintColorRes = R.color.red_negative
            )
            cleanCategory.contains("entertainment") -> CategoryStyle(
                iconRes = R.drawable.ic_subscriptions,
                bgDrawableRes = R.drawable.bg_icon_circle_pink,
                tintColorRes = R.color.red_negative
            )
            cleanCategory.contains("electronics") || cleanCategory.contains("laptop") -> CategoryStyle(
                iconRes = R.drawable.ic_laptop,
                bgDrawableRes = R.drawable.bg_icon_circle_blue,
                tintColorRes = R.color.primary_blue
            )
            cleanCategory.contains("saving") || cleanCategory.contains("piggy") -> CategoryStyle(
                iconRes = R.drawable.ic_piggy_bank,
                bgDrawableRes = R.drawable.bg_icon_circle_green,
                tintColorRes = R.color.green_positive
            )
            else -> CategoryStyle(
                iconRes = R.drawable.ic_wallet,
                bgDrawableRes = R.drawable.bg_icon_circle_blue,
                tintColorRes = R.color.primary_blue
            )
        }
    }
}
