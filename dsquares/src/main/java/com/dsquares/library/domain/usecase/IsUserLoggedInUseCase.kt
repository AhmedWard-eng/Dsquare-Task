package com.dsquares.library.domain.usecase

import com.dsquares.library.data.local.TokenManager

class IsUserLoggedInUseCase(
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Boolean {
        return tokenManager.getUserId() != null
    }
}