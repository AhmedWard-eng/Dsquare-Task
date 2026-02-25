package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.ILoginRepo
import com.dsquares.library.domain.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private val loginRepo = mockk<ILoginRepo>()
    private val validatePhoneUseCase = mockk<ValidatePhoneUseCase>()
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        useCase = LoginUseCase(loginRepo, validatePhoneUseCase)
    }

    // ── Happy path ───────────────────────────────────────────────────────

    @Test
    fun `given valid phone and successful login, when invoked, then returns Success`() = runTest {
        every { validatePhoneUseCase("01012345678") } returns Result.Success(Unit)
        coEvery { loginRepo.login("01012345678") } returns Result.Success(Unit)

        val result = useCase("01012345678")

        assertTrue(result is Result.Success)
        verify { validatePhoneUseCase("01012345678") }
        coVerify { loginRepo.login("01012345678") }
    }

    // ── Validation failure ───────────────────────────────────────────────

    @Test
    fun `given invalid phone, when invoked, then returns validation failure without calling repo`() = runTest {
        val validationFailure = Result.Failure(DomainException.InvalidPhoneNumberException())
        every { validatePhoneUseCase("123") } returns validationFailure

        val result = useCase("123")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
        coVerify(exactly = 0) { loginRepo.login(any()) }
    }

    // ── Repo failure ─────────────────────────────────────────────────────

    @Test
    fun `given valid phone but repo returns LoginFailed, when invoked, then returns LoginFailedException`() = runTest {
        every { validatePhoneUseCase("01012345678") } returns Result.Success(Unit)
        coEvery { loginRepo.login("01012345678") } returns Result.Failure(
            DomainException.LoginFailedException("Login failed")
        )

        val result = useCase("01012345678")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.LoginFailedException)
        assertEquals("Login failed", result.exception.message)
    }

    @Test
    fun `given valid phone but repo returns NoConnectivity, when invoked, then returns NoConnectivityException`() = runTest {
        every { validatePhoneUseCase("01012345678") } returns Result.Success(Unit)
        coEvery { loginRepo.login("01012345678") } returns Result.Failure(
            DomainException.NoConnectivityException()
        )

        val result = useCase("01012345678")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NoConnectivityException)
    }

    @Test
    fun `given valid phone but repo returns NetworkException, when invoked, then returns NetworkException`() = runTest {
        every { validatePhoneUseCase("01012345678") } returns Result.Success(Unit)
        coEvery { loginRepo.login("01012345678") } returns Result.Failure(
            DomainException.NetworkException(RuntimeException("timeout"))
        )

        val result = useCase("01012345678")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NetworkException)
    }

    @Test
    fun `given valid phone but repo returns TokenStorageException, when invoked, then returns TokenStorageException`() = runTest {
        every { validatePhoneUseCase("01012345678") } returns Result.Success(Unit)
        coEvery { loginRepo.login("01012345678") } returns Result.Failure(
            DomainException.TokenStorageException()
        )

        val result = useCase("01012345678")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.TokenStorageException)
    }
}