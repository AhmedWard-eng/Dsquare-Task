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

        if (token != null) {
            // TODO: Replace hardcoded "Bearer" with tokenType from TokenManager once the BE fixes
            //  the login response to return "Bearer" instead of "JWT"
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(authenticatedRequest)
        }

        return chain.proceed(request)
    }
}
