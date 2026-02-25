package com.dsquares.library.data.network.model.login

data class LoginResult(
    val tokenType: String?,
    val accessToken: String?,
    val expiresInMins: Int?,
    val refreshToken: String?
)