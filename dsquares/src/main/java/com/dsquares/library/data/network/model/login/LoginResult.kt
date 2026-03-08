package com.dsquares.library.data.network.model.login

import com.google.gson.annotations.SerializedName

data class LoginResult(
    @SerializedName("tokenType")
    val tokenType: String?,
    @SerializedName("accessToken")
    val accessToken: String?,
    @SerializedName("expiresInMins")
    val expiresInMins: Int?,
    @SerializedName("refreshToken")
    val refreshToken: String?
)