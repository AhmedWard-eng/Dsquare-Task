package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.Result
import java.util.UUID

class LoginByPhoneNumberUseCase(
    private val loginByUuidUseCase: LoginByUuidUseCase,
    private val validatePhoneUseCase: ValidatePhoneUseCase
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return when (val phoneValidationResult = validatePhoneUseCase(phone)) {
            is Result.Failure -> phoneValidationResult
            is Result.Success -> loginByUuidUseCase(generateUuidFromPhoneNumber(phone))
        }
    }
    private fun generateUuidFromPhoneNumber(phone: String): String =  UUID.nameUUIDFromBytes(phone.toByteArray()).toString()
}
