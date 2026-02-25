package com.dsquares.library.domain.usecase

import com.dsquares.library.data.local.TokenManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsUserLoggedInUseCaseTest {

    private val tokenManager = mockk<TokenManager>()
    private lateinit var useCase: IsUserLoggedInUseCase

    @Before
    fun setup() {
        useCase = IsUserLoggedInUseCase(tokenManager)
    }

    @Test
    fun `given user id exists, when invoked, then returns true`() = runTest {
        coEvery { tokenManager.getUserId() } returns "user-123"

        val result = useCase()

        assertTrue(result)
        coVerify { tokenManager.getUserId() }
    }

    @Test
    fun `given user id is null, when invoked, then returns false`() = runTest {
        coEvery { tokenManager.getUserId() } returns null

        val result = useCase()

        assertFalse(result)
        coVerify { tokenManager.getUserId() }
    }
}