package com.adhyantacore.expensetracker.presentation.transactions

import com.adhyantacore.expensetracker.domain.model.Categories


enum class TransactionFilter(val label: String, val category: String?) {
    ALL("All", null),
    FOOD("Food", Categories.FOOD),
    TRANSPORT("Transport", Categories.TRANSPORT),
    SHOPPING("Shopping", Categories.SHOPPING),
    ENTERTAINMENT("Entertainment", Categories.ENTERTAINMENT),
    BILLS("Bills", Categories.BILLS),
    HEALTH("Health", Categories.HEALTH),
    EDUCATION("Education", Categories.EDUCATION),
    TRAVEL("Travel", Categories.TRAVEL),
    RENT("Rent", Categories.RENT),
    BUSINESS("Business", Categories.BUSINESS),
    GIFT("Gift", Categories.GIFT),
    MOBILE_RECHARGE("Mobile Recharge", Categories.MOBILE_RECHARGE),
    ELECTRONICS("Electronics", Categories.ELECTRONICS),
    PETS("Pets", Categories.PETS),
    OTHERS("Others", Categories.OTHERS)
}