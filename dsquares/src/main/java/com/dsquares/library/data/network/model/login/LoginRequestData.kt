package com.dsquares.library.data.network.model.login


import com.google.gson.annotations.SerializedName

data class LoginRequestData(
    @SerializedName("UserIdentifier")
    val userIdentifier: String?
)