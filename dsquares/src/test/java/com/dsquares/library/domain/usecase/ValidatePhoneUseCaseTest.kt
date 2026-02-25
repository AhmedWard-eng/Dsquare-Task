package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePhoneUseCaseTest {

    private val useCase = ValidatePhoneUseCase()

    // ── Valid phones (010, 011, 012, 015 + 8 digits) ─────────────────────

    @Test
    fun `given phone starting with 010 and 8 digits, when invoked, then returns Success`() {
        val result = useCase("01012345678")
        assertTrue(result is Result.Success)
    }

    @Test
    fun `given phone starting with 011 and 8 digits, when invoked, then returns Success`() {
        val result = useCase("01112345678")
        assertTrue(result is Result.Success)
    }

    @Test
    fun `given phone starting with 012 and 8 digits, when invoked, then returns Success`() {
        val result = useCase("01212345678")
        assertTrue(result is Result.Success)
    }

    @Test
    fun `given phone starting with 015 and 8 digits, when invoked, then returns Success`() {
        val result = useCase("01512345678")
        assertTrue(result is Result.Success)
    }

    // ── Invalid prefix ───────────────────────────────────────────────────

    @Test
    fun `given phone starting with 013, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("01312345678")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    @Test
    fun `given phone starting with 014, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("01412345678")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    @Test
    fun `given phone starting with 016, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("01612345678")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    // ── Invalid length ───────────────────────────────────────────────────

    @Test
    fun `given phone with fewer than 11 digits, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("0101234567")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    @Test
    fun `given phone with more than 11 digits, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("010123456789")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    // ── Edge cases ───────────────────────────────────────────────────────

    @Test
    fun `given empty string, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    @Test
    fun `given phone with letters, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("010abcdefgh")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    @Test
    fun `given phone with spaces, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("010 1234 5678")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }

    @Test
    fun `given phone with country code prefix, when invoked, then returns InvalidPhoneNumberException`() {
        val result = useCase("+201012345678")
        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).exception is DomainException.InvalidPhoneNumberException)
    }
}