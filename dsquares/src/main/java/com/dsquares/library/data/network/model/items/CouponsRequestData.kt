package com.dsquares.library.data.network.model.items

import com.google.gson.annotations.SerializedName
data class CouponsRequestData(
    @SerializedName("page")
    val page: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("categoryCode")
    val categoryCode: String,
    @SerializedName("rewardTypes")
    val rewardTypes: List<String>
)