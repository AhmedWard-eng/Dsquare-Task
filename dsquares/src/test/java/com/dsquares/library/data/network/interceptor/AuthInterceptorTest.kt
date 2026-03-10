package com.dsquares.library.data.network.interceptor

import com.dsquares.library.data.local.TokenManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private lateinit var interceptor: AuthInterceptor
    private val chain = mockk<Interceptor.Chain>()
    private val requestSlot = slot<Request>()

    @Before
    fun setup() {
        interceptor = AuthInterceptor(tokenManager)
    }

    private fun stubChain(url: String = "https://api.example.com/api/DynamicApp/v1/Integration/Items") {
        val originalRequest = Request.Builder().url(url).build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }
    }

    @Test
    fun `given valid token exists, when intercepting request, then Authorization header is Bearer plus token`() {
        coEvery { tokenManager.getAccessToken() } returns "my-access-token"
        stubChain()

        interceptor.intercept(chain)

        assertEquals("Bearer my-access-token", requestSlot.captured.header("Authorization"))
    }

    @Test
    fun `given no token stored, when intercepting request, then no Authorization header is added`() {
        coEvery { tokenManager.getAccessToken() } returns null
        stubChain()

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Authorization"))
    }

    @Test
    fun `given login endpoint, when intercepting request, then Authorization header is skipped`() {
        coEvery { tokenManager.getAccessToken() } returns "my-token"
        stubChain("https://api.example.com/api/DynamicApp/v1/Integration/Token")

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Authorization"))
    }
}
