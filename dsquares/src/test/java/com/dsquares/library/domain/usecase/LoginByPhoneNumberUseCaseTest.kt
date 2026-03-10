package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class LoginByPhoneNumberUseCaseTest {

    private val loginByUuidUseCase = mockk<LoginByUuidUseCase>()
    private val validatePhoneUseCase = mockk<ValidatePhoneUseCase>()
    private lateinit var useCase: LoginByPhoneNumberUseCase

    @Before
    fun setup() {
        useCase = LoginByPhoneNumberUseCase(loginByUuidUseCase, validatePhoneUseCase)
    }

    // ── Happy path ───────────────────────────────────────────────────────

    @Test
    fun `given valid phone and successful login, when invoked, then returns Success`() = runTest {
        val phone = "01012345678"
        val expectedUuid = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(expectedUuid) } returns Result.Success(Unit)

        val result = useCase(phone)

        assertTrue(result is Result.Success)
        verify { validatePhoneUseCase(phone) }
        coVerify { loginByUuidUseCase(expectedUuid) }
    }

    // ── UUID determinism ─────────────────────────────────────────────────

    @Test
    fun `given same phone, when invoked twice, then generates the same UUID both times`() = runTest {
        val phone = "01012345678"
        val uuidSlot = mutableListOf<String>()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(capture(uuidSlot)) } returns Result.Success(Unit)

        useCase(phone)
        useCase(phone)

        assertEquals(2, uuidSlot.size)
        assertEquals(uuidSlot[0], uuidSlot[1])
    }

    @Test
    fun `given different phones, when invoked, then generates different UUIDs`() = runTest {
        val phone1 = "01012345678"
        val phone2 = "01098765432"
        val uuidSlot = mutableListOf<String>()

        every { validatePhoneUseCase(any()) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(capture(uuidSlot)) } returns Result.Success(Unit)

        useCase(phone1)
        useCase(phone2)

        assertEquals(2, uuidSlot.size)
        assertTrue(uuidSlot[0] != uuidSlot[1])
    }

    @Test
    fun `given a phone, when invoked, then passes UUID derived from phone to loginByUuidUseCase`() = runTest {
        val phone = "01012345678"
        val expectedUuid = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()
        val uuidSlot = slot<String>()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(capture(uuidSlot)) } returns Result.Success(Unit)

        useCase(phone)

        assertEquals(expectedUuid, uuidSlot.captured)
    }

    // ── Validation failure ───────────────────────────────────────────────

    @Test
    fun `given invalid phone, when invoked, then returns validation failure without calling loginByUuidUseCase`() = runTest {
        val validationFailure = Result.Failure(DomainException.InvalidPhoneNumberException())
        every { validatePhoneUseCase("123") } returns validationFailure

        val result = useCase("123")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
        coVerify(exactly = 0) { loginByUuidUseCase(any()) }
    }

    // ── Repo failures (propagated via loginByUuidUseCase) ────────────────

    @Test
    fun `given valid phone but login returns LoginFailed, when invoked, then returns LoginFailedException`() = runTest {
        val phone = "01012345678"
        val expectedUuid = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(expectedUuid) } returns Result.Failure(
            DomainException.LoginFailedException("Login failed")
        )

        val result = useCase(phone)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.LoginFailedException)
        assertEquals("Login failed", result.exception.message)
    }

    @Test
    fun `given valid phone but login returns NoConnectivity, when invoked, then returns NoConnectivityException`() = runTest {
        val phone = "01012345678"
        val expectedUuid = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(expectedUuid) } returns Result.Failure(
            DomainException.NoConnectivityException()
        )

        val result = useCase(phone)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NoConnectivityException)
    }

    @Test
    fun `given valid phone but login returns NetworkException, when invoked, then returns NetworkException`() = runTest {
        val phone = "01012345678"
        val expectedUuid = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(expectedUuid) } returns Result.Failure(
            DomainException.NetworkException(RuntimeException("timeout"))
        )

        val result = useCase(phone)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.NetworkException)
    }

    @Test
    fun `given valid phone but login returns TokenStorageException, when invoked, then returns TokenStorageException`() = runTest {
        val phone = "01012345678"
        val expectedUuid = UUID.nameUUIDFromBytes(phone.toByteArray()).toString()

        every { validatePhoneUseCase(phone) } returns Result.Success(Unit)
        coEvery { loginByUuidUseCase(expectedUuid) } returns Result.Failure(
            DomainException.TokenStorageException()
        )

        val result = useCase(phone)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.TokenStorageException)
    }
}