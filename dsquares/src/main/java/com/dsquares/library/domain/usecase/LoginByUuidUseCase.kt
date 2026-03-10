package com.dsquares.library.domain.usecase

import com.dsquares.library.domain.ILoginRepo
import com.dsquares.library.domain.Result

class LoginByUuidUseCase(private val loginRepo: ILoginRepo) {
    suspend operator fun invoke(uuid: String): Result<Unit> {
        return loginRepo.login(uuid)
    }
}