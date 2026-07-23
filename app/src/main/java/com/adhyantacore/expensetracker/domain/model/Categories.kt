package com.adhyantacore.expensetracker.domain.model


object Categories {
    const val FOOD = "🍔 Food"
    const val TRANSPORT = "🚕 Transport"
    const val SHOPPING = "🛒 Shopping"
    const val ENTERTAINMENT = "🎬 Entertainment"
    const val BILLS = "💡 Bills"
    const val HEALTH = "🏥 Health"
    const val EDUCATION = "📚 Education"
    const val TRAVEL = "✈️ Travel"
    const val RENT = "🏠 Rent"
    const val BUSINESS = "💼 Business"
    const val GIFT = "🎁 Gift"
    const val MOBILE_RECHARGE = "📱 Mobile Recharge"
    const val ELECTRONICS = "💻 Electronics"
    const val PETS = "🐶 Pets"
    const val OTHERS = "📦 Others"

    val ALL = arrayOf(
        FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, BILLS, HEALTH,
        EDUCATION, TRAVEL, RENT, BUSINESS, GIFT, MOBILE_RECHARGE,
        ELECTRONICS, PETS, OTHERS
    )
}