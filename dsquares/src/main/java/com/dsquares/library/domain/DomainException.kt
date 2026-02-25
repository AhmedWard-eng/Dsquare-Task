package com.dsquares.library.domain

sealed class DomainException(message: String) : Exception(message) {
    class LoginFailedException(message: String) : DomainException(message)
    class TokenStorageException : DomainException("Failed to save authentication token")
    class NoConnectivityException : DomainException("No internet connection available")
    class NetworkException(cause: Throwable) : DomainException("Network error: ${cause.message}")
    class UnknownException(cause: Throwable) : DomainException("Unexpected error: ${cause.message}")
    class InvalidPhoneNumberException : DomainException("Please enter a valid phone number consisting of 11 digits starting with 010, 011, 012, or 015")
}
