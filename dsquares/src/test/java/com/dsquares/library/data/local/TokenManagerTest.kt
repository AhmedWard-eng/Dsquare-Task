package com.dsquares.library.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.dsquares.library.security.CryptoManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class TokenManagerTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val cryptoManager = mockk<CryptoManager>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createDataStore(name: String = "test"): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            File(tmpFolder.newFolder(), "$name.preferences_pb")
        }

    private fun tokenManagerWithNullDataStore() =
        TokenManager(dataStore = null, cryptoManager = cryptoManager, ioDispatcher = testDispatcher)

    private fun tokenManagerWithDataStore(dataStore: DataStore<Preferences> = createDataStore()) =
       TokenManager(dataStore = dataStore, cryptoManager = cryptoManager, ioDispatcher = testDispatcher)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ── Null DataStore tests ──────────────────────────────────────────────

    @Test
    fun `given null dataStore, when saveToken is called, then returns false`() = runTest {
        val tokenManager = tokenManagerWithNullDataStore()
        val result = tokenManager.saveToken("access", "refresh", "Bearer", 30, "user-1")
        assertFalse(result)
    }

    @Test
    fun `given null dataStore, when getAccessToken is called, then returns null`() = runTest {
        val tokenManager = tokenManagerWithNullDataStore()
        assertNull(tokenManager.getAccessToken())
    }

    @Test
    fun `given null dataStore, when getTokenType is called, then returns null`() = runTest {
        val tokenManager = tokenManagerWithNullDataStore()
        assertNull(tokenManager.getTokenType())
    }

    @Test
    fun `given null dataStore, when getUserId is called, then returns null`() = runTest {
        val tokenManager = tokenManagerWithNullDataStore()
        assertNull(tokenManager.getUserId())
    }

    @Test
    fun `given null dataStore, when isTokenExpired is called, then returns true`() = runTest {
        val tokenManager = tokenManagerWithNullDataStore()
        assertTrue(tokenManager.isTokenExpired())
    }

    @Test
    fun `given null dataStore, when clearToken is called, then returns false`() = runTest {
        val tokenManager = tokenManagerWithNullDataStore()
        assertFalse(tokenManager.clearToken())
    }

    // ── Encryption failure tests ─────────────────────────────────────────

    @Test
    fun `given encrypt returns null for accessToken, when saveToken is called, then returns false`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt("access") } returns null
        every { cryptoManager.encrypt("refresh") } returns "enc-refresh"
        every { cryptoManager.encrypt("user-1") } returns "enc-user"

        assertFalse(tokenManager.saveToken("access", "refresh", "Bearer", 30, "user-1"))
    }

    @Test
    fun `given encrypt returns null for refreshToken, when saveToken is called, then returns false`() = runTest {
        val  tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt("access") } returns "enc-access"
        every { cryptoManager.encrypt("refresh") } returns null
        every { cryptoManager.encrypt("user-1") } returns "enc-user"

        assertFalse(tokenManager.saveToken("access", "refresh", "Bearer", 30, "user-1"))
    }

    @Test
    fun `given encrypt returns null for userId, when saveToken is called, then returns false`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt("access") } returns "enc-access"
        every { cryptoManager.encrypt("refresh") } returns "enc-refresh"
        every { cryptoManager.encrypt("user-1") } returns null

        assertFalse(tokenManager.saveToken("access", "refresh", "Bearer", 30, "user-1"))
    }

    @Test
    fun `given valid inputs, when saveToken is called, then encrypt is called for accessToken, refreshToken, and userId`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt("my-access") } returns "enc-access"
        every { cryptoManager.encrypt("my-refresh") } returns "enc-refresh"
        every { cryptoManager.encrypt("my-user") } returns "enc-user"

        tokenManager.saveToken("my-access", "my-refresh", "Bearer", 30, "my-user")

        verify { cryptoManager.encrypt("my-access") }
        verify { cryptoManager.encrypt("my-refresh") }
        verify { cryptoManager.encrypt("my-user") }
    }

    // ── End-to-end tests (save → read round-trip) ────────────────────────

    @Test
    fun `given token is saved, when reading back, then accessToken, tokenType, and userId are correct`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt("my-access") } returns "enc-access"
        every { cryptoManager.encrypt("my-refresh") } returns "enc-refresh"
        every { cryptoManager.encrypt("user-42") } returns "enc-user-42"
        every { cryptoManager.decrypt("enc-access") } returns "my-access"
        every { cryptoManager.decrypt("enc-user-42") } returns "user-42"

        val saved = tokenManager.saveToken("my-access", "my-refresh", "Bearer", 60, "user-42")

        assertTrue(saved)
        assertEquals("my-access", tokenManager.getAccessToken())
        assertEquals("Bearer", tokenManager.getTokenType())
        assertEquals("user-42", tokenManager.getUserId())
    }

    @Test
    fun `given token saved with future expiry, when checking expiry, then isTokenExpired returns false`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt(any()) } returns "encrypted"

        tokenManager.saveToken("a", "r", "Bearer", 60, "u")

        assertFalse(tokenManager.isTokenExpired())
    }

    @Test
    fun `given token is saved, when clearToken is called, then reads return null and isTokenExpired returns true`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt(any()) } returns "encrypted"

        tokenManager.saveToken("a", "r", "Bearer", 60, "u")
        val cleared = tokenManager.clearToken()

        assertTrue(cleared)
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getTokenType())
        assertNull(tokenManager.getUserId())
        assertTrue(tokenManager.isTokenExpired())
    }

    @Test
    fun `given token saved twice, when reading back, then latest values are returned`() = runTest {
        val tokenManager = tokenManagerWithDataStore()
        every { cryptoManager.encrypt("access-1") } returns "enc-1"
        every { cryptoManager.encrypt("access-2") } returns "enc-2"
        every { cryptoManager.encrypt(any()) } returns "enc-other"
        every { cryptoManager.decrypt("enc-2") } returns "access-2"

        tokenManager.saveToken("access-1", "r", "Bearer", 60, "u")

        every { cryptoManager.encrypt("access-2") } returns "enc-2"
        tokenManager.saveToken("access-2", "r", "Custom", 120, "u")

        assertEquals("access-2", tokenManager.getAccessToken())
        assertEquals("Custom", tokenManager.getTokenType())
    }
}