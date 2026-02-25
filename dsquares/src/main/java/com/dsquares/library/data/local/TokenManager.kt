package com.dsquares.library.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dsquares.library.di.ServiceLocator.TAG
import com.dsquares.library.security.CryptoManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class TokenManager(
    private val dataStore: DataStore<Preferences>?,
    private val cryptoManager: CryptoManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val DATASTORE_NOT_AVAILABLE =
            "DataStore is not available. Ensure DSquareSDK.init(context) is called with a valid Context before using the SDK."
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_TYPE_KEY = stringPreferencesKey("token_type")
        private val EXPIRES_AT_KEY = longPreferencesKey("expires_at")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    private fun requireDataStore(): DataStore<Preferences>? {
        if (dataStore == null) Log.d(TAG, DATASTORE_NOT_AVAILABLE)
        return dataStore
    }

    private suspend fun <T> execute(block: suspend () -> T): T =
        withContext(ioDispatcher) { block() }

    suspend fun saveToken(
        accessToken: String,
        refreshToken: String,
        tokenType: String,
        expiresInMins: Int,
        userId: String
    ): Boolean {
        val store = requireDataStore() ?: return false

        val encryptedAccess = execute { cryptoManager.encrypt(accessToken) }
        val encryptedRefresh = execute { cryptoManager.encrypt(refreshToken) }
        val encryptedUserId = execute { cryptoManager.encrypt(userId) }

        if (encryptedAccess == null || encryptedRefresh == null || encryptedUserId == null) {
            Log.e(TAG, "Failed to encrypt tokens. This may indicate a problem with the Android Keystore on this device.")
            return false
        }

        val expiresAt = System.currentTimeMillis() + (expiresInMins * 60L * 1000L)
        store.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = encryptedAccess
            prefs[REFRESH_TOKEN_KEY] = encryptedRefresh
            prefs[TOKEN_TYPE_KEY] = tokenType
            prefs[EXPIRES_AT_KEY] = expiresAt
            prefs[USER_ID_KEY] = encryptedUserId
        }
        return true
    }

    suspend fun getAccessToken(): String? {
        val store = requireDataStore() ?: return null
        return try {
            store.data.map { prefs ->
                prefs[ACCESS_TOKEN_KEY]?.let { execute { cryptoManager.decrypt(it) } }
            }.first()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read access token", e)
            null
        }
    }

    suspend fun getTokenType(): String? {
        val store = requireDataStore() ?: return null
        return try {
            store.data.map { prefs ->
                prefs[TOKEN_TYPE_KEY]
            }.first()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read token type", e)
            null
        }
    }

    suspend fun getUserId(): String? {
        val store = requireDataStore() ?: return null
        return try {
            store.data.map { prefs ->
                prefs[USER_ID_KEY]?.let { execute { cryptoManager.decrypt(it) } }
            }.first()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read user ID", e)
            null
        }
    }

    suspend fun isTokenExpired(): Boolean {
        val store = requireDataStore() ?: return true
        return try {
            val expiresAt = store.data.map { prefs ->
                prefs[EXPIRES_AT_KEY]
            }.first() ?: return true
            System.currentTimeMillis() >= expiresAt
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check token expiry", e)
            true
        }
    }

    suspend fun clearToken(): Boolean {
        val store = requireDataStore() ?: return false
        return try {
            store.edit { it.clear() }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear token", e)
            false
        }
    }
}