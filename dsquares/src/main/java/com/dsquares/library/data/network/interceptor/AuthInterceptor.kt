package com.dsquares.library.data.network.interceptor

import com.dsquares.library.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath.contains("Integration/Token")) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenManager.getAccessToken() }
        val tokenType = runBlocking { tokenManager.getTokenType() } ?: "Bearer"

        if (token != null) {
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", "$tokenType $token")
                .build()
            return chain.proceed(authenticatedRequest)
        }

        return chain.proceed(request)
    }
}
