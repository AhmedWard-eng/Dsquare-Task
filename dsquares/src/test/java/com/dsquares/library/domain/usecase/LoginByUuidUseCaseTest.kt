package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.ILoginRepo
import com.dsquares.library.domain.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginByUuidUseCaseTest {

    private val loginRepo = mockk<ILoginRepo>()
    private lateinit var useCase: LoginByUuidUseCase

    @Before
    fun setup() {
        useCase = LoginByUuidUseCase(loginRepo)
    }

    // ── Happy path ────────────────────────────────────────────────────────

    @Test
    fun `given successful login, when invoked, then returns Success`() = runTest {
        val uuid = "some-uuid"
        coEvery { loginRepo.login(uuid) } returns Result.Success(Unit)

        val result = useCase(uuid)

        assertTrue(result is Result.Success)
        coVerify { loginRepo.login(uuid) }
    }

    @Test
    fun `given a uuid, when invoked, then passes it unchanged to repo`() = runTest {
        val uuidSlot = slot<String>()
        val uuid = "fixed-uuid-value"
        coEvery { loginRepo.login(capture(uuidSlot)) } returns Result.Success(Unit)

        useCase(uuid)

        assertEquals(uuid, uuidSlot.captured)
    }

    // ── Repo failures ─────────────────────────────────────────────────────

    @Test
    fun `given repo returns LoginFailed, when invoked, then returns LoginFailedException`() = runTest {
        val uuid = "some-uuid"
        coEvery { loginRepo.login(uuid) } returns Result.Failure(
            DomainException.LoginFailedException("Login failed")
        )

        val result = useCase(uuid)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.LoginFailedException)
        assertEquals("Login failed", result.exception.message)
    }

    @Test
    fun `given repo returns NoConnectivity, when invoked, then returns NoConnectivityException`() = runTest {
        val uuid = "some-uuid"
        coEvery { loginRepo.login(uuid) } returns Result.Failure(
            DomainException.NoConnectivityException()
        )

        val result = useCase(uuid)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NoConnectivityException)
    }

    @Test
    fun `given repo returns NetworkException, when invoked, then returns NetworkException`() = runTest {
        val uuid = "some-uuid"
        coEvery { loginRepo.login(uuid) } returns Result.Failure(
            DomainException.NetworkException(RuntimeException("timeout"))
        )

        val result = useCase(uuid)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NetworkException)
    }

    @Test
    fun `given repo returns TokenStorageException, when invoked, then returns TokenStorageException`() = runTest {
        val uuid = "some-uuid"
        coEvery { loginRepo.login(uuid) } returns Result.Failure(
            DomainException.TokenStorageException()
        )

        val result = useCase(uuid)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.TokenStorageException)
    }
}