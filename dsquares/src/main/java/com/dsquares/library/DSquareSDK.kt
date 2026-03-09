package com.dsquares.library

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_FIRST_USER
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.dsquares.library.data.repo.LoginRepo
import com.dsquares.library.di.AppContainer
import com.dsquares.library.constants.TAG
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.Result
import com.dsquares.library.domain.usecase.LoginUseCase
import com.dsquares.library.domain.usecase.ValidatePhoneUseCase
import com.dsquares.library.ui.DSquareActivity
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

    companion object {

        @Volatile
        private var _appContainer: AppContainer? = null

        internal val appContainer: AppContainer
            get() = _appContainer
                ?: throw IllegalStateException("DSquareSDK.init() must be called before using the SDK")

        private var _loginUseCase: LoginUseCase? = null
        private val loginUseCase: LoginUseCase
            get() = _loginUseCase ?: LoginUseCase(
                loginRepo = LoginRepo(appContainer.remoteSource, appContainer.tokenManager),
                validatePhoneUseCase = ValidatePhoneUseCase()
            ).also { _loginUseCase = it }

        /**
         * Initializes the SDK. Must be called exactly once, typically in
         * [Application.onCreate].
         *
         * Subsequent calls are ignored and a warning is logged.
         *
         * @param application the host [Application] instance.
         * @param apiKey the API key issued by DSquare. Must not be blank.
         */
        @JvmStatic
        fun init(application: Application, apiKey: String) {
            if (_appContainer != null) {
                Log.w(TAG, "DSquareSDK is already initialized")
                return
            }
            if (apiKey.isBlank()) {
                Log.e(TAG, "API key must not be blank")
                return
            }
            _appContainer = AppContainer(application.applicationContext, apiKey)
        }

        internal fun checkInitialized(): Boolean {
            if (_appContainer == null) {
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
            return appContainer.tokenManager.clearToken()
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
        fun ShowCoupons(onResult: (CouponsResult) -> Unit) {
            val context = LocalContext.current
            val updatedOnResult by rememberUpdatedState(onResult)
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    when (result.resultCode) {
                        RESULT_OK -> {
                            updatedOnResult(CouponsResult.Success("Coupon applied successfully"))
                        }

                        RESULT_CANCELED -> {
                            updatedOnResult(CouponsResult.Canceled)
                        }

                        RESULT_FIRST_USER -> {
                            updatedOnResult(CouponsResult.Failure())
                        }
                    }
                }
            if (!checkInitialized()) return
            LaunchedEffect(Unit) {
                launcher.launch(Intent(context, DSquareActivity::class.java))
            }
        }

        /**
         * Remembers and returns a [ManagedActivityResultLauncher] that delivers
         * [CouponsResult] when the coupons flow finishes.
         *
         * Use this when you need to control **when** the coupons screen is
         * launched (e.g. on a button click) rather than launching it
         * immediately on composition like [ShowCoupons].
         *
         * Pass the returned launcher to [launchCoupons] to start the flow:
         *
         * ```kotlin
         * // Inside setContent { }
         * val launcher = DSquareSDK.rememberLauncherForDSquareSDK { result ->
         *     // handle CouponsResult
         * }
         *
         * Button(onClick = {
         *     if (launcher != null) DSquareSDK.launchCoupons(activity, launcher)
         * }) { Text("Show Coupons") }
         * ```
         *
         * @param onResult callback invoked with a [CouponsResult] when the
         *                 coupons flow finishes.
         * @return a [ManagedActivityResultLauncher] to pass to [launchCoupons],
         *         or `null` if the SDK has not been initialized.
         */
        @Composable
        fun rememberLauncherForDSquareSDK(
            onResult: (CouponsResult) -> Unit
        ): ManagedActivityResultLauncher<Intent, ActivityResult>? {
            val updatedOnResult by rememberUpdatedState(onResult)
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    when (result.resultCode) {
                        RESULT_OK -> {
                            updatedOnResult(CouponsResult.Success("Coupon applied successfully"))
                        }

                        RESULT_CANCELED -> {
                            updatedOnResult(CouponsResult.Canceled)
                        }

                        RESULT_FIRST_USER -> {
                            updatedOnResult(CouponsResult.Failure())
                        }
                    }
                }
            if (!checkInitialized()) return null
            return launcher
        }

        /**
         * Registers an [ActivityResultLauncher] that will deliver [CouponsResult]
         * when the coupons flow finishes.
         *
         * **Must be called during the activity's `CREATED` state** (i.e. in
         * `onCreate` before `super.onCreate` or in an initializer block) —
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
         *         or `null` if the SDK has not been initialized.
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
         *         or `null` if the SDK has not been initialized.
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
            launcher.launch(Intent(activity, DSquareActivity::class.java))
        }


        /**
         * Launches the coupons screen in a fire-and-forget manner.
         *
         * @param activity the [Activity] used to build the launch [Intent].
         */
        @JvmStatic
        fun launchCoupons(activity: Activity) {
            if (!checkInitialized()) return
            activity.startActivity(Intent(activity, DSquareActivity::class.java))
        }


        @VisibleForTesting
        internal fun resetForTesting(
            loginUseCase: LoginUseCase? = null,
            appContainer: AppContainer? = null
        ) {
            _appContainer = appContainer
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
