package com.dsquares.library.data.network.model.items

import com.google.gson.annotations.SerializedName

data class Denomination(
    @SerializedName("brand")
    val brand: String?,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("code")
    val code: String?,
    @SerializedName("denominationType")
    val denominationType: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("discount")
    val discount: String?,
    @SerializedName("from")
    val from: Int?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("inStock")
    val inStock: Boolean?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("points")
    val points: Int?,
    @SerializedName("redemptionChannel")
    val redemptionChannel: String?,
    @SerializedName("redemptionFactor")
    val redemptionFactor: Int?,
    @SerializedName("termsAndConditions")
    val termsAndConditions: String?,
    @SerializedName("to")
    val to: Int?,
    @SerializedName("usageInstructions")
    val usageInstructions: String?,
    @SerializedName("value")
    val value: Int?
)
