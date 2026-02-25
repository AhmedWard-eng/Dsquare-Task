package com.dsquares.library.domain.usecase

import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.di.ServiceLocator

class IsUserLoggedInUseCase(
    private val tokenManager: TokenManager = ServiceLocator.tokenManager
) {
    suspend operator fun invoke(): Boolean {
        return tokenManager.getUserId() != null
    }
}