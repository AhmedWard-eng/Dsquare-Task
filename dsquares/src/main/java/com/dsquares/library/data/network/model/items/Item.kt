package com.dsquares.library.data.network.model.items

data class Item(
    val code: String?,
    val denominations: List<Denomination>?,
    val description: String?,
    val imageUrl: String?,
    val locked: Boolean?,
    val name: String?,
    val rewardType: String?
)