package com.dsquares.library

/**
 * Error codes returned by the SDK to describe the type of failure.
 */
enum class ErrorCode {
    /** The phone number provided is invalid (must be exactly 11 digits). */
    INVALID_PHONE,
    /** The device has no active internet connection. */
    NO_INTERNET,
    /** A network-level error occurred (timeout, DNS failure, etc.). */
    NETWORK_ERROR,
    /** The login request was rejected by the server. */
    LOGIN_FAILED,
    /** An unexpected error occurred. */
    UNKNOWN
}

/**
 * Represents the outcome of a [DSquareSDK.logIn] call.
 */
sealed class LoginResult {
    /** Login completed successfully and the session token has been stored. */
    object Success : LoginResult()

    /**
     * Login failed.
     *
     * @property code categorised error type.
     * @property message human-readable description of the failure.
     */
    data class Error(val code: ErrorCode, val message: String) : LoginResult()
}

/**
 * Represents the outcome of the coupons flow launched via
 * [DSquareSDK.ShowCoupons], [DSquareSDK.registerCouponsLauncher], or [DSquareSDK.launchCoupons].
 */
sealed class CouponsResult {
    /**
     * The coupons flow completed successfully.
     *
     * @property data a confirmation message or coupon payload.
     */
    data class Success(val data: String) : CouponsResult()

    /**
     * The coupons flow failed.
     *
     * @property message optional description of the failure.
     */
    data class Failure(val message: String? = null) : CouponsResult()

    /** The user dismissed the coupons screen without completing the flow. */
    object Canceled : CouponsResult()
}
