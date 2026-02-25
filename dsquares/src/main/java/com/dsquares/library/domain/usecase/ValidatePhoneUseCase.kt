package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result

class ValidatePhoneUseCase {

    private val phoneRegex = Regex("^(010|011|012|015)\\d{8}$")

    operator fun invoke(phone: String): Result<Unit> {
        return if (phoneRegex.matches(phone)) {
            Result.Success(Unit)
        } else {
            Result.Failure(DomainException.InvalidPhoneNumberException())
        }
    }
}