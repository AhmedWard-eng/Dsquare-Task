package com.dsquares.library.domain

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val exception: DomainException) : Result<Nothing>
}