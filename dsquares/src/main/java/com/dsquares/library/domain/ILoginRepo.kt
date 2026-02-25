package com.dsquares.library.domain

interface ILoginRepo {
    suspend fun login(userId: String): Result<Unit>
}

