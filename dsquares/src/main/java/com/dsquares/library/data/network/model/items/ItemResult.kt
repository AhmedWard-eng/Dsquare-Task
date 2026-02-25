package com.dsquares.library.data.network.model.items

data class ItemResult(
    val items: List<Item>?,
    val totalItems: Int?,
    val totalPages: Int?
)