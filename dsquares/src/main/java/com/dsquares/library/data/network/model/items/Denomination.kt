package com.dsquares.library.data.network.model.items

data class Denomination(
    val brand: String?,
    val categories: List<String>?,
    val code: String?,
    val denominationType: String?,
    val description: String?,
    val discount: String?,
    val from: Int?,
    val imageUrl: String?,
    val inStock: Boolean?,
    val name: String?,
    val points: Int?,
    val redemptionChannel: String?,
    val redemptionFactor: Int?,
    val termsAndConditions: String?,
    val to: Int?,
    val usageInstructions: String?,
    val value: Int?
)