package com.dsquares.library.data.network.model

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("errors")
    val errors: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("referenceCode")
    val referenceCode: String?,
    @SerializedName("result")
    val result: T?,
    @SerializedName("statusCode")
    val statusCode: Int?,
    @SerializedName("statusName")
    val statusName: String?
)