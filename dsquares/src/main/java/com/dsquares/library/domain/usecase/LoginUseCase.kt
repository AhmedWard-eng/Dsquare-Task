package com.dsquares.library.domain.usecase

import com.dsquares.library.data.repo.LoginRepo
import com.dsquares.library.domain.ILoginRepo
import com.dsquares.library.domain.Result

class LoginUseCase(
    private val loginRepo: ILoginRepo = LoginRepo(),
    private val validatePhoneUseCase: ValidatePhoneUseCase = ValidatePhoneUseCase()
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return when (val phoneValidationResult = validatePhoneUseCase(phone)) {
            is Result.Failure -> phoneValidationResult
            is Result.Success -> loginRepo.login(phone)
        }
    }
}
