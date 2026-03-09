package com.dsquares.library.data.network.interceptor

import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.login.LoginResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private val remoteSource = mockk<IRemoteSource>()
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setup() {
        authenticator = TokenAuthenticator(tokenManager) { remoteSource }
    }

    private fun build401Response(
        request: Request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .build()
    ): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
    }

    private fun loginResult(
        accessToken: String? = "new-access",
        refreshToken: String? = "new-refresh",
        tokenType: String? = "Bearer",
        expiresInMins: Int? = 60
    ) = BaseResponse(
        errors = null,
        message = null,
        referenceCode = null,
        result = LoginResult(
            tokenType = tokenType,
            accessToken = accessToken,
            expiresInMins = expiresInMins,
            refreshToken = refreshToken
        ),
        statusCode = 200,
        statusName = null
    )

    @Test
    fun `given 401 with valid userId, when authenticating, then token is refreshed and request retried`() {
        coEvery { tokenManager.getAccessToken() } returns "expired-token"
        coEvery { tokenManager.isTokenExpired() } returns true
        coEvery { tokenManager.getUserId() } returns "user123"
        coEvery { remoteSource.login("user123") } returns loginResult()

        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNotNull(retryRequest)
        assertEquals("Bearer new-access", retryRequest!!.header("Authorization"))
        assertEquals("true", retryRequest.header("Authorization-Retry"))
        coVerify {
            tokenManager.saveToken(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                tokenType = "Bearer",
                expiresInMins = 60,
                userId = "user123"
            )
        }
    }

    @Test
    fun `given another thread already refreshed token, when authenticating, then uses new token without re-login`() {
        // The request had "old-token" but tokenManager now has "already-refreshed-token"
        coEvery { tokenManager.getAccessToken() } returns "already-refreshed-token"
        coEvery { tokenManager.isTokenExpired() } returns false

        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer old-token")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNotNull(retryRequest)
        assertEquals("Bearer already-refreshed-token", retryRequest!!.header("Authorization"))
        coVerify(exactly = 0) { remoteSource.login(any()) }
    }

    @Test
    fun `given Authorization-Retry header already present, when authenticating, then returns null to stop loop`() {
        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer some-token")
            .header("Authorization-Retry", "true")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNull(retryRequest)
        coVerify(exactly = 0) { tokenManager.getAccessToken() }
        coVerify(exactly = 0) { remoteSource.login(any()) }
    }

    @Test
    fun `given null userId, when authenticating, then returns null without refreshing`() {
        coEvery { tokenManager.getAccessToken() } returns "expired-token"
        coEvery { tokenManager.isTokenExpired() } returns true
        coEvery { tokenManager.getUserId() } returns null

        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNull(retryRequest)
        coVerify(exactly = 0) { remoteSource.login(any()) }
    }

    @Test
    fun `given login returns null result fields, when authenticating, then returns null without saving`() {
        coEvery { tokenManager.getAccessToken() } returns "expired-token"
        coEvery { tokenManager.isTokenExpired() } returns true
        coEvery { tokenManager.getUserId() } returns "user123"
        coEvery { remoteSource.login("user123") } returns loginResult(
            accessToken = null,
            refreshToken = null,
            tokenType = null,
            expiresInMins = null
        )

        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNull(retryRequest)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given login returns null result, when authenticating, then returns null`() {
        coEvery { tokenManager.getAccessToken() } returns "expired-token"
        coEvery { tokenManager.isTokenExpired() } returns true
        coEvery { tokenManager.getUserId() } returns "user123"
        coEvery { remoteSource.login("user123") } returns BaseResponse(
            errors = null,
            message = null,
            referenceCode = null,
            result = null,
            statusCode = 401,
            statusName = null
        )

        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNull(retryRequest)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given login returns partial null fields, when authenticating, then returns null without saving`() {
        coEvery { tokenManager.getAccessToken() } returns "expired-token"
        coEvery { tokenManager.isTokenExpired() } returns true
        coEvery { tokenManager.getUserId() } returns "user123"
        // accessToken present but refreshToken null
        coEvery { remoteSource.login("user123") } returns loginResult(
            accessToken = "new-access",
            refreshToken = null,
            tokenType = "Bearer",
            expiresInMins = 60
        )

        val request = Request.Builder()
            .url("https://api.example.com/api/DynamicApp/v1/Integration/Items")
            .header("Authorization", "Bearer expired-token")
            .build()
        val response = build401Response(request)

        val retryRequest = authenticator.authenticate(null, response)

        assertNull(retryRequest)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }
}