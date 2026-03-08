package com.dsquares.library.data.network.model.items

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("code")
    val code: String?,
    @SerializedName("denominations")
    val denominations: List<Denomination>?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("locked")
    val locked: Boolean?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("rewardType")
    val rewardType: String?
)