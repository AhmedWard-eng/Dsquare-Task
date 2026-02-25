package com.dsquares.library.data.network.model

data class BaseResponse<T>(
    val errors: String?,
    val message: String?,
    val referenceCode: String?,
    val result: T?,
    val statusCode: Int?,
    val statusName: String?
)