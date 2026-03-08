package com.dsquares.library.data.repo

import android.util.Log
import com.dsquares.library.data.local.TokenManager
import com.dsquares.library.data.network.IRemoteSource
import com.dsquares.library.data.network.RemoteSource
import com.dsquares.library.domain.DomainException
import com.dsquares.library.domain.ILoginRepo
import com.dsquares.library.domain.Result
import com.dsquares.library.data.network.interceptor.NoConnectivityException
import com.dsquares.library.di.ServiceLocator
import com.dsquares.library.di.ServiceLocator.TAG
import retrofit2.HttpException
import java.io.IOException

class LoginRepo(
    private val remoteSource: IRemoteSource = RemoteSource(),
    private val tokenManager: TokenManager = ServiceLocator.tokenManager
) : ILoginRepo {

    override suspend fun login(userId: String): Result<Unit> {
        return try {
            val response = remoteSource.login(userId)
            val result = response.result

            if (result == null ||
                result.accessToken == null ||
                result.tokenType == null ||
                result.expiresInMins == null
            ) {
                val errorMessage = response.errors
                    ?: response.message
                    ?: "Login failed: invalid or missing token data in response"
                return Result.Failure(DomainException.LoginFailedException(errorMessage))
            }

            val saved = tokenManager.saveToken(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken.orEmpty(),
                tokenType = result.tokenType,
                expiresInMins = result.expiresInMins,
                userId = userId
            )

            if (!saved) {
                return Result.Failure(DomainException.TokenStorageException())
            }

            Result.Success(Unit)
        } catch (_: NoConnectivityException) {
            Result.Failure(DomainException.NoConnectivityException())
        } catch (e: IOException) {
            Result.Failure(DomainException.NetworkException(e))
        } catch (e: HttpException) {
            Result.Failure(DomainException.HttpException(e.extractErrorMessage()))
        } catch (e: Exception) {
            Log.d(TAG, "Failed to fetch items: ${e.message}")
            Result.Failure(DomainException.UnknownException(e))
        }
    }
}