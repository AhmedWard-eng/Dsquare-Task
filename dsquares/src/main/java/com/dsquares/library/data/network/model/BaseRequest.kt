package com.dsquares.library.data.network.model

import com.google.gson.annotations.SerializedName

data class BaseRequest<T>(
    @SerializedName("data")
    val data: T?
)