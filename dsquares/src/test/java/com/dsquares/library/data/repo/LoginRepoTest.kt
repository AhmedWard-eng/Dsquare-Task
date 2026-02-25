package com.dsquares.library.data.repo

import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.interceptor.NoConnectivityException
import com.dsquares.library.data.network.model.BaseResponse
import com.dsquares.library.data.network.model.login.LoginResult
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class LoginRepoTest {

    private val remoteSource = mockk<IRemoteSource>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private lateinit var loginRepo: LoginRepo

    @Before
    fun setup() {
        loginRepo = LoginRepo(remoteSource, tokenManager)
    }

    private fun successResponse(
        accessToken: String? = "access-123",
        refreshToken: String? = "refresh-456",
        tokenType: String? = "Bearer",
        expiresInMins: Int? = 30
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
        statusName = "OK"
    )

    // ── Happy path ──────────────────────────────────────────────────────

    @Test
    fun `given successful login, when login is called, then token is saved and Success returned`() = runTest {
        coEvery { remoteSource.login("user-1") } returns successResponse()
        coEvery { tokenManager.saveToken(any(), any(), any(), any(), any()) } returns true

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Success)
        coVerify {
            tokenManager.saveToken(
                accessToken = "access-123",
                refreshToken = "refresh-456",
                tokenType = "Bearer",
                expiresInMins = 30,
                userId = "user-1"
            )
        }
    }

    @Test
    fun `given null refreshToken in response, when login is called, then empty string is saved for refreshToken`() = runTest {
        coEvery { remoteSource.login("user-1") } returns successResponse(refreshToken = null)
        coEvery { tokenManager.saveToken(any(), any(), any(), any(), any()) } returns true

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Success)
        coVerify {
            tokenManager.saveToken(
                accessToken = "access-123",
                refreshToken = "",
                tokenType = "Bearer",
                expiresInMins = 30,
                userId = "user-1"
            )
        }
    }

    // ── Token validation failures ───────────────────────────────────────

    @Test
    fun `given null result in response, when login is called, then LoginFailedException with default message`() = runTest {
        coEvery { remoteSource.login("user-1") } returns BaseResponse(
            errors = null, message = null, referenceCode = null,
            result = null, statusCode = 401, statusName = "Unauthorized"
        )

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        val exception = (result as Result.Failure).exception
        assertTrue(exception is DomainException.LoginFailedException)
        assertEquals("Login failed: invalid or missing token data in response", exception.message)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given null result with errors field, when login is called, then LoginFailedException uses errors message`() = runTest {
        coEvery { remoteSource.login("user-1") } returns BaseResponse(
            errors = "Invalid credentials", message = "Something else", referenceCode = null,
            result = null, statusCode = 401, statusName = null
        )

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        val exception = (result as Result.Failure).exception
        assertTrue(exception is DomainException.LoginFailedException)
        assertEquals("Invalid credentials", exception.message)
    }

    @Test
    fun `given null result with only message field, when login is called, then LoginFailedException uses message`() = runTest {
        coEvery { remoteSource.login("user-1") } returns BaseResponse(
            errors = null, message = "Bad request", referenceCode = null,
            result = null, statusCode = 400, statusName = null
        )

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        assertEquals("Bad request", (result as Result.Failure).exception.message)
    }

    @Test
    fun `given null accessToken in result, when login is called, then LoginFailedException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } returns successResponse(accessToken = null)

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.LoginFailedException)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given null tokenType in result, when login is called, then LoginFailedException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } returns successResponse(tokenType = null)

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.LoginFailedException)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `given null expiresInMins in result, when login is called, then LoginFailedException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } returns successResponse(expiresInMins = null)

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.LoginFailedException)
        coVerify(exactly = 0) { tokenManager.saveToken(any(), any(), any(), any(), any()) }
    }

    // ── Token storage failure ───────────────────────────────────────────

    @Test
    fun `given saveToken returns false, when login is called, then TokenStorageException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } returns successResponse()
        coEvery { tokenManager.saveToken(any(), any(), any(), any(), any()) } returns false

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.TokenStorageException)
    }

    // ── Network / exception handling ────────────────────────────────────

    @Test
    fun `given NoConnectivityException, when login is called, then NoConnectivityException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } throws NoConnectivityException()

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NoConnectivityException)
    }

    @Test
    fun `given IOException, when login is called, then NetworkException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } throws IOException("timeout")

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        val exception = (result as Result.Failure).exception
        assertTrue(exception is DomainException.NetworkException)
        assertEquals("Network error: timeout", exception.message)
    }

    @Test
    fun `given generic exception, when login is called, then UnknownException returned`() = runTest {
        coEvery { remoteSource.login("user-1") } throws RuntimeException("unexpected")

        val result = loginRepo.login("user-1")

        assertTrue(result is Result.Failure)
        val exception = (result as Result.Failure).exception
        assertTrue(exception is DomainException.UnknownException)
        assertEquals("Unexpected error: unexpected", exception.message)
    }
}