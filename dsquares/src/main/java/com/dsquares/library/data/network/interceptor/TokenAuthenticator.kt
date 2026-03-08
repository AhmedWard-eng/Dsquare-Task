package com.dsquares.library.data.network.interceptor

import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.RemoteSource
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val remoteSourceProvider: () -> IRemoteSource = { RemoteSource() }
) : Authenticator {

    private val remoteSource by lazy { remoteSourceProvider() }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite retry loops — if we already tried refreshing, give up
        if (response.request.header("Authorization-Retry") != null) return null

        synchronized(this) {
            val currentToken = runBlocking { tokenManager.getAccessToken() }
            val isTokenExpired = runBlocking { tokenManager.isTokenExpired() }

            // Another thread already refreshed — retry with the new token
            if (!isTokenExpired && currentToken != null) {
                // TODO: Replace hardcoded "Bearer" with result.tokenType once the BE fixes
                //  the login response to return "Bearer" instead of "JWT"
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .header("Authorization-Retry", "true")
                    .build()
            }

            // We need to refresh
            val userId = runBlocking { tokenManager.getUserId() } ?: return null
            val loginResponse = runBlocking { remoteSource.login(userId) }
            val result = loginResponse.result ?: return null

            if (result.accessToken == null || result.tokenType == null ||
                result.refreshToken == null || result.expiresInMins == null
            ) return null

            runBlocking {
                tokenManager.saveToken(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    tokenType = result.tokenType,
                    expiresInMins = result.expiresInMins,
                    userId = userId
                )
            }

            // TODO: Replace hardcoded "Bearer" with result.tokenType once the BE fixes
            //  the login response to return "Bearer" instead of "JWT"
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${result.accessToken}")
                .header("Authorization-Retry", "true")
                .build()
        }
    }
}