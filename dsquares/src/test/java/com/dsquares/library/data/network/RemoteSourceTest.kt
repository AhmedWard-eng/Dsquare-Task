package com.dsquares.library.data.network

import com.dsquares.library.data.network.api.ApiService
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.items.ItemResult
import com.dsquares.library.data.network.model.login.LoginResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteSourceTest {

    private val apiService = mockk<ApiService>()
    private lateinit var remoteSource: RemoteSource

    @Before
    fun setup() {
        remoteSource = RemoteSource(apiService)
    }

    // ── login ────────────────────────────────────────────────────────────

    @Test
    fun `given a userId, when login is called, then delegates to apiService with correct body`() = runTest {
        val expected = BaseResponse(
            errors = null,
            message = null,
            referenceCode = null,
            result = LoginResult("Bearer", "access-tok", 30, "refresh-tok"),
            statusCode = 200,
            statusName = "OK"
        )
        coEvery { apiService.login(any()) } returns expected

        val result = remoteSource.login("user-42")

        assertEquals(expected, result)
        coVerify { apiService.login(mapOf("UserIdentifier" to "user-42")) }
    }

    @Test
    fun `given apiService throws, when login is called, then exception is propagated`() = runTest {
        coEvery { apiService.login(any()) } throws RuntimeException("server error")

        val exception = runCatching { remoteSource.login("user-1") }.exceptionOrNull()

        assertEquals("server error", exception?.message)
    }

    // ── getItems ─────────────────────────────────────────────────────────

    @Test
    fun `given all parameters, when getItems is called, then delegates all parameters to apiService`() = runTest {
        val expected = BaseResponse(
            errors = null,
            message = null,
            referenceCode = null,
            result = ItemResult(items = emptyList(), totalItems = 0, totalPages = 0),
            statusCode = 200,
            statusName = "OK"
        )
        coEvery { apiService.getItems(any(), any(), any(), any(), any()) } returns expected

        val result = remoteSource.getItems(
            page = 1,
            pageSize = 10,
            name = "coffee",
            categoryCode = "CAT-1",
            rewardTypes = listOf("COUPON", "VOUCHER")
        )

        assertEquals(expected, result)
        coVerify {
            apiService.getItems(
                page = 1,
                pageSize = 10,
                name = "coffee",
                categoryCode = "CAT-1",
                rewardTypes = listOf("COUPON", "VOUCHER")
            )
        }
    }

    @Test
    fun `given null optional parameters, when getItems is called, then passes nulls to apiService`() = runTest {
        val expected = BaseResponse(
            errors = null,
            message = null,
            referenceCode = null,
            result = ItemResult(items = emptyList(), totalItems = 0, totalPages = 0),
            statusCode = 200,
            statusName = "OK"
        )
        coEvery { apiService.getItems(any(), any(), any(), any(), any()) } returns expected

        val result = remoteSource.getItems(
            page = 2,
            pageSize = 20,
            name = "",
            categoryCode = null,
            rewardTypes = null
        )

        assertEquals(expected, result)
        coVerify {
            apiService.getItems(
                page = 2,
                pageSize = 20,
                name = "",
                categoryCode = null,
                rewardTypes = null
            )
        }
    }

    @Test
    fun `given apiService throws, when getItems is called, then exception is propagated`() = runTest {
        coEvery { apiService.getItems(any(), any(), any(), any(), any()) } throws RuntimeException("timeout")

        val exception = runCatching {
            remoteSource.getItems(1, 10, "", null, null)
        }.exceptionOrNull()

        assertEquals("timeout", exception?.message)
    }
}