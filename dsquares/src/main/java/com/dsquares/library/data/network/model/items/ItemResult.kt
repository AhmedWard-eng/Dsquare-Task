package com.dsquares.library.data.network.model.items

import com.google.gson.annotations.SerializedName

data class ItemResult(
    @SerializedName("items")
    val items: List<Item>?,
    @SerializedName("totalItems")
    val totalItems: Int?,
    @SerializedName("totalPages")
    val totalPages: Int?
)