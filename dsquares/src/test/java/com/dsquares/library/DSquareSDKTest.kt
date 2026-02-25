package com.dsquares.library

import android.app.Application
import android.util.Log
import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.di.ServiceLocator
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import com.dsquares.library.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DSquareSDKTest {

    private val mockApplication = mockk<Application>(relaxed = true)
    private val mockTokenManager = mockk<TokenManager>()
    private val mockLoginUseCase = mockk<LoginUseCase>()

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockkObject(ServiceLocator)
        every { ServiceLocator.tokenManager } returns mockTokenManager

        DSquareSDK.resetForTesting(mockLoginUseCase)
    }

    @After
    fun tearDown() {
        DSquareSDK.resetForTesting()
        unmockkAll()
    }

    // ── init() ──────────────────────────────────────────────────────────

    @Test
    fun `given valid apiKey, when init is called, then SDK is initialized`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "valid-key")

        coEvery { mockLoginUseCase.invoke(any()) } returns Result.Success(Unit)

        val result = DSquareSDK.logIn("01234567890")

        assertEquals(LoginResult.Success, result)
    }

    @Test
    fun `given blank apiKey, when init is called, then SDK remains uninitialized`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "   ")

        val result = DSquareSDK.logIn("01234567890")

        assertEquals(LoginResult.Error(ErrorCode.UNKNOWN, "SDK not initialized"), result)
        verify(exactly = 0) { ServiceLocator.apiKey = any() }
        coVerify(exactly = 0) { mockLoginUseCase.invoke(any()) }
    }

    @Test
    fun `given SDK already initialized, when init is called again, then second call is ignored`() {
        DSquareSDK.init(mockApplication, apiKey = "key-1")
        DSquareSDK.init(mockApplication, apiKey = "key-2")

        verify(exactly = 1) { ServiceLocator.apiKey = any() }
        verify { ServiceLocator.apiKey = "key-1" }
    }

    // ── logIn() ─────────────────────────────────────────────────────────

    @Test
    fun `given SDK not initialized, when logIn is called, then returns Error UNKNOWN and LoginUseCase is never called`() = runTest {
        val result = DSquareSDK.logIn("01234567890")

        assertEquals(LoginResult.Error(ErrorCode.UNKNOWN, "SDK not initialized"), result)
        coVerify(exactly = 0) { mockLoginUseCase.invoke(any()) }
    }

    @Test
    fun `given LoginUseCase returns Success, when logIn is called, then returns LoginResult Success`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        coEvery { mockLoginUseCase.invoke("01234567890") } returns Result.Success(Unit)

        val result = DSquareSDK.logIn("01234567890")

        assertEquals(LoginResult.Success, result)
        coVerify(exactly = 1) { mockLoginUseCase.invoke("01234567890") }
    }

    @Test
    fun `given LoginUseCase returns InvalidPhoneNumberException, when logIn is called, then returns INVALID_PHONE`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        val exception = DomainException.InvalidPhoneNumberException()
        coEvery { mockLoginUseCase.invoke(any()) } returns Result.Failure(exception)

        val result = DSquareSDK.logIn("123")

        assertTrue(result is LoginResult.Error)
        assertEquals(ErrorCode.INVALID_PHONE, (result as LoginResult.Error).code)
    }

    @Test
    fun `given LoginUseCase returns NoConnectivityException, when logIn is called, then returns NO_INTERNET`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        val exception = DomainException.NoConnectivityException()
        coEvery { mockLoginUseCase.invoke(any()) } returns Result.Failure(exception)

        val result = DSquareSDK.logIn("01234567890")

        assertTrue(result is LoginResult.Error)
        assertEquals(ErrorCode.NO_INTERNET, (result as LoginResult.Error).code)
    }

    @Test
    fun `given LoginUseCase returns NetworkException, when logIn is called, then returns NETWORK_ERROR`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        val exception = DomainException.NetworkException(RuntimeException("timeout"))
        coEvery { mockLoginUseCase.invoke(any()) } returns Result.Failure(exception)

        val result = DSquareSDK.logIn("01234567890")

        assertTrue(result is LoginResult.Error)
        assertEquals(ErrorCode.NETWORK_ERROR, (result as LoginResult.Error).code)
    }

    @Test
    fun `given LoginUseCase returns LoginFailedException, when logIn is called, then returns LOGIN_FAILED`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        val exception = DomainException.LoginFailedException("Invalid credentials")
        coEvery { mockLoginUseCase.invoke(any()) } returns Result.Failure(exception)

        val result = DSquareSDK.logIn("01234567890")

        assertTrue(result is LoginResult.Error)
        assertEquals(ErrorCode.LOGIN_FAILED, (result as LoginResult.Error).code)
    }

    @Test
    fun `given LoginUseCase returns UnknownException, when logIn is called, then returns UNKNOWN`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        val exception = DomainException.UnknownException(RuntimeException("unexpected"))
        coEvery { mockLoginUseCase.invoke(any()) } returns Result.Failure(exception)

        val result = DSquareSDK.logIn("01234567890")

        assertTrue(result is LoginResult.Error)
        assertEquals(ErrorCode.UNKNOWN, (result as LoginResult.Error).code)
    }

    // ── logout() ────────────────────────────────────────────────────────

    @Test
    fun `given SDK not initialized, when logout is called, then returns false and TokenManager is never called`() = runTest {
        val result = DSquareSDK.logout()

        assertFalse(result)
        coVerify(exactly = 0) { mockTokenManager.clearToken() }
    }

    // ── Not-initialized guard for all public APIs ────────────────────────

    @Test
    fun `given SDK not initialized, when logInBlocking is called, then returns Error UNKNOWN and LoginUseCase is never called`() {
        val result = DSquareSDK.logInBlocking("01234567890")

        assertEquals(LoginResult.Error(ErrorCode.UNKNOWN, "SDK not initialized"), result)
        coVerify(exactly = 0) { mockLoginUseCase.invoke(any()) }
    }

    @Test
    fun `given SDK not initialized, when logoutBlocking is called, then returns false and TokenManager is never called`() {
        val result = DSquareSDK.logoutBlocking()

        assertFalse(result)
        coVerify(exactly = 0) { mockTokenManager.clearToken() }
    }

    @Test
    fun `given clearToken returns true, when logout is called, then returns true`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        coEvery { mockTokenManager.clearToken() } returns true

        val result = DSquareSDK.logout()

        assertTrue(result)
        coVerify(exactly = 1) { mockTokenManager.clearToken() }
    }

    @Test
    fun `given clearToken returns false, when logout is called, then returns false`() = runTest {
        DSquareSDK.init(mockApplication, apiKey = "key")
        coEvery { mockTokenManager.clearToken() } returns false

        val result = DSquareSDK.logout()

        assertFalse(result)
    }
}
