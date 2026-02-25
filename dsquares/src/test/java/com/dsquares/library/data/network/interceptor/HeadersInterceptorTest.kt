package com.dsquares.library.data.network.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class HeadersInterceptorTest {

    private val chain = mockk<Interceptor.Chain>()
    private val requestSlot = slot<Request>()
    private var testLocale: Locale = Locale.ENGLISH

    private val interceptor by lazy {
        HeadersInterceptor(apiKey = "test-api-key", localeProvider = { testLocale })
    }

    private fun stubChain(url: String = "https://api.example.com/items") {
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
    fun `given any request, when intercepted, then Content-Type header is application json`() {
        stubChain()
        interceptor.intercept(chain)
        assertEquals("application/json", requestSlot.captured.header("Content-Type"))
    }

    @Test
    fun `given api key configured, when intercepted, then x-api-key header is set`() {
        stubChain()
        interceptor.intercept(chain)
        assertEquals("test-api-key", requestSlot.captured.header("x-api-key"))
    }

    @Test
    fun `given device locale is Arabic, when intercepted, then Accept-Language is ar`() {
        testLocale = Locale.forLanguageTag("ar")
        stubChain()
        interceptor.intercept(chain)
        assertEquals("ar", requestSlot.captured.header("Accept-Language"))
    }

    @Test
    fun `given device locale changes to French, when intercepted, then Accept-Language is fr`() {
        testLocale = Locale.forLanguageTag("fr")
        stubChain()
        interceptor.intercept(chain)
        assertEquals("fr", requestSlot.captured.header("Accept-Language"))
    }
}
