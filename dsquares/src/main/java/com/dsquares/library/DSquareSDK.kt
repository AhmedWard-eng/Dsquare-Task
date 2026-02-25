package com.dsquares.library

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_FIRST_USER
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.dsquares.library.di.ServiceLocator
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import com.dsquares.library.domain.usecase.LoginUseCase
import com.dsquares.library.ui.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Entry point for the DSquare SDK.
 *
 * All public API is exposed through the [Companion] object.
 * Call [init] once from [Application.onCreate] before using any other method.
 *
 * ```kotlin
 * // Application class
 * DSquareSDK.init(this, apiKey = "your-api-key")
 *
 * // Kotlin coroutine
 * val result = DSquareSDK.logIn("+20123456789")
 *
 * // Java / blocking
 * LoginResult result = DSquareSDK.logInBlocking("+20123456789");
 * ```
 */
class DSquareSDK {

    private fun configure(application: Application, apiKey: String) {
        ServiceLocator.appContext = application.applicationContext
        ServiceLocator.apiKey = apiKey
    }

    companion object {
        private const val TAG = "DSquareSDK"
        private val dSquareSDK = DSquareSDK()

        @Volatile
        private var initialized = false
        private var _loginUseCase: LoginUseCase? = null
        private val loginUseCase: LoginUseCase
            get() = _loginUseCase ?: LoginUseCase().also { _loginUseCase = it }

        /**
         * Initialises the SDK. Must be called exactly once, typically in
         * [Application.onCreate].
         *
         * Subsequent calls are ignored and a warning is logged.
         *
         * @param application the host [Application] instance.
         * @param apiKey the API key issued by DSquare. Must not be blank.
         */
        @JvmStatic
        fun init(application: Application, apiKey: String) {
            if (initialized) {
                Log.w(TAG, "DSquareSDK is already initialized")
                return
            }
            if (apiKey.isBlank()) {
                Log.e(TAG, "API key must not be blank")
                return
            }
            dSquareSDK.configure(application, apiKey)
            initialized = true
        }

        private fun checkInitialized(): Boolean {
            if (!initialized) {
                Log.e(TAG, "DSquareSDK.init() must be called before using the SDK")
                return false
            }
            return true
        }

        /**
         * Authenticates a user with the given phone number.
         *
         * This is a **suspend** function — call it from a coroutine scope.
         * For Java or blocking usage see [logInBlocking].
         *
         * @param phone the user's phone number (must be exactly 11 digits).
         * @return [LoginResult.Success] on success, or [LoginResult.Error] with
         *         an [ErrorCode] and a human-readable message on failure.
         */
        suspend fun logIn(phone: String): LoginResult {
            if (!checkInitialized()) return LoginResult.Error(
                ErrorCode.UNKNOWN,
                "SDK not initialized"
            )
            return when (val result = loginUseCase.invoke(phone)) {
                is Result.Success -> LoginResult.Success
                is Result.Failure -> LoginResult.Error(
                    code = result.exception.toErrorCode(),
                    message = result.exception.message ?: "Login failed"
                )
            }
        }

        /**
         * Blocking variant of [logIn] for use on a background thread or from Java.
         *
         * **Must not be called on the main thread.**
         *
         * @param phone the user's phone number (must be exactly 11 digits).
         * @return [LoginResult.Success] on success, or [LoginResult.Error] on failure.
         * @see logIn
         */
        @WorkerThread
        @JvmStatic
        fun logInBlocking(phone: String): LoginResult = runBlocking { logIn(phone) }

        /**
         * Clears the stored session token, effectively logging the user out.
         *
         * This is a **suspend** function — call it from a coroutine scope.
         * For Java or blocking usage see [logoutBlocking].
         *
         * @return `true` if the token was cleared successfully, `false` otherwise.
         */
        suspend fun logout(): Boolean {
            if (!checkInitialized()) return false
            return ServiceLocator.tokenManager.clearToken()
        }

        /**
         * Blocking variant of [logout] for use on a background thread or from Java.
         *
         * **Must not be called on the main thread.**
         *
         * @return `true` if the token was cleared successfully, `false` otherwise.
         * @see logout
         */
        @WorkerThread
        @JvmStatic
        fun logoutBlocking(): Boolean = runBlocking { logout() }

        /**
         * Jetpack Compose helper that launches the coupons screen automatically
         * when this composable enters the composition.
         *
         * The result is delivered asynchronously via [onResult].
         *
         * @param onResult callback invoked with a [CouponsResult] when the
         *                 coupons flow finishes.
         */
        @Composable
        fun showCoupons(onResult: (CouponsResult) -> Unit) {
            if (!checkInitialized()) return
            val context = LocalContext.current
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    when (result.resultCode) {
                        RESULT_OK -> {
                            onResult(CouponsResult.Success("Coupon applied successfully"))
                        }

                        RESULT_CANCELED -> {
                            onResult(CouponsResult.Canceled)
                        }

                        RESULT_FIRST_USER -> {
                            onResult(CouponsResult.Failure())
                        }
                    }
                }
            LaunchedEffect(Unit) {
                launcher.launch(Intent(context, MainActivity::class.java))
            }
        }

        /**
         * Registers an [ActivityResultLauncher] that will deliver [CouponsResult]
         * when the coupons flow finishes.
         *
         * **Must be called during the activity's `CREATED` state** (i.e. in
         * `onCreate` before `super.onCreate` or in an initialiser block) —
         * the same rule as [ComponentActivity.registerForActivityResult].
         *
         * Use the returned launcher with [launchCoupons] to start the flow.
         *
         * ```kotlin
         * // In onCreate
         * private val couponsLauncher = DSquareSDK.run {
         *     registerCouponsLauncher { result -> handleResult(result) }
         * }
         *
         * // Later, on button click
         * DSquareSDK.launchCoupons(this, couponsLauncher)
         * ```
         *
         * @param onResult callback invoked with a [CouponsResult] when the
         *                 coupons flow finishes.
         * @return an [ActivityResultLauncher] to pass to [launchCoupons],
         *         or `null` if the SDK has not been initialised.
         */
        @JvmName("registerCouponsLauncherExt")
        fun ComponentActivity.registerCouponsLauncher(
            onResult: (CouponsResult) -> Unit
        ): ActivityResultLauncher<Intent>? {
            if (!checkInitialized()) return null
            return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> onResult(CouponsResult.Success("Coupon applied successfully"))
                    RESULT_CANCELED -> onResult(CouponsResult.Canceled)
                    RESULT_FIRST_USER -> onResult(CouponsResult.Failure())
                }
            }
        }

        /**
         * Java-friendly variant of [registerCouponsLauncher] that does not
         * require Kotlin extension-function syntax.
         *
         * **Must be called during the activity's `CREATED` state.**
         *
         * ```java
         * // In onCreate
         * ActivityResultLauncher<Intent> launcher =
         *     DSquareSDK.registerCouponsLauncher(this, result -> {
         *         // handle result
         *     });
         *
         * // Later, on button click
         * DSquareSDK.launchCoupons(this, launcher);
         * ```
         *
         * @param activity the [ComponentActivity] to register the launcher on.
         * @param onResult callback invoked with a [CouponsResult] when the
         *                 coupons flow finishes.
         * @return an [ActivityResultLauncher] to pass to [launchCoupons],
         *         or `null` if the SDK has not been initialised.
         */
        @JvmStatic
        fun registerCouponsLauncher(
            activity: ComponentActivity,
            onResult: (CouponsResult) -> Unit
        ): ActivityResultLauncher<Intent>? {
            return activity.registerCouponsLauncher(onResult)
        }

        /**
         * Launches the coupons screen using a previously registered [launcher].
         *
         * @param activity the [ComponentActivity] used to build the launch [Intent].
         * @param launcher the launcher returned by [registerCouponsLauncher].
         * @see registerCouponsLauncher
         */
        @JvmStatic
        fun launchCoupons(activity: ComponentActivity, launcher: ActivityResultLauncher<Intent>) {
            if (!checkInitialized()) return
            launcher.launch(Intent(activity, MainActivity::class.java))
        }

        @VisibleForTesting
        internal fun resetForTesting(loginUseCase: LoginUseCase? = null) {
            initialized = false
            _loginUseCase = loginUseCase
        }

        private fun DomainException.toErrorCode(): ErrorCode = when (this) {
            is DomainException.InvalidPhoneNumberException -> ErrorCode.INVALID_PHONE
            is DomainException.NoConnectivityException -> ErrorCode.NO_INTERNET
            is DomainException.NetworkException -> ErrorCode.NETWORK_ERROR
            is DomainException.LoginFailedException -> ErrorCode.LOGIN_FAILED
            else -> ErrorCode.UNKNOWN
        }
    }
}
