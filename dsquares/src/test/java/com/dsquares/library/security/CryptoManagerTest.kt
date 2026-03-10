package com.dsquares.library.security

import android.util.Base64
import android.util.Log
import com.dsquares.library.constants.TAG
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManagerTest {

    private val keyProvider = mockk<KeyStoreProvider>()
    private val mockKey = mockk<SecretKey>()
    private val mockCipher = mockk<Cipher>()
    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setup() {
        mockkStatic(Cipher::class)
        mockkStatic(Base64::class)
        mockkStatic(Log::class)

        every { keyProvider.getKey() } returns mockKey
        every { Cipher.getInstance(CryptoManager.TRANSFORMATION) } returns mockCipher
        every { Log.d(any(), any()) } returns 0

        cryptoManager = CryptoManager(keyProvider)
    }

    @After
    fun tearDown() {
        unmockkStatic(Cipher::class)
        unmockkStatic(Base64::class)
        unmockkStatic(Log::class)
    }

    // ── encrypt ─────────────────────────────────────────────────────────

    @Test
    fun `given valid plaintext, when encrypt is called, then uses correct transformation and encrypt mode`() {
        val fakeIv = ByteArray(12) { 1 }
        val fakeEncrypted = ByteArray(20) { 2 }

        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockKey) } just Runs
        every { mockCipher.iv } returns fakeIv
        every { mockCipher.doFinal(any<ByteArray>()) } returns fakeEncrypted
        every { Base64.encodeToString(any<ByteArray>(), Base64.NO_WRAP) } returns "encoded"

        val result = cryptoManager.encrypt("test")

        assertEquals("encoded", result)
        verify { Cipher.getInstance(CryptoManager.TRANSFORMATION) }
        verify { mockCipher.init(Cipher.ENCRYPT_MODE, mockKey) }
        verify { mockCipher.doFinal("test".toByteArray(Charsets.UTF_8)) }
        verify(exactly = 0) { Log.d(any(), any()) }
    }

    @Test
    fun `given valid plaintext, when encrypt is called, then combines IV and encrypted data before Base64 encoding`() {
        val fakeIv = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val fakeEncrypted = byteArrayOf(20, 21, 22)
        val combinedSlot = slot<ByteArray>()

        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockKey) } just Runs
        every { mockCipher.iv } returns fakeIv
        every { mockCipher.doFinal(any<ByteArray>()) } returns fakeEncrypted
        every { Base64.encodeToString(capture(combinedSlot), eq(Base64.NO_WRAP)) } returns "encoded"

        cryptoManager.encrypt("x")

        val combined = combinedSlot.captured
        assertArrayEquals(fakeIv, combined.copyOfRange(0, 12))
        assertArrayEquals(fakeEncrypted, combined.copyOfRange(12, combined.size))
    }

    @Test
    fun `given keyProvider throws, when encrypt is called, then returns null and logs error`() {
        every { keyProvider.getKey() } throws RuntimeException("no key")

        val result = cryptoManager.encrypt("test")

        assertNull(result)
        verify { Log.d(TAG, "Failed to encrypt: no key") }
    }

    @Test
    fun `given cipher doFinal throws, when encrypt is called, then returns null and logs error`() {
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockKey) } just Runs
        every { mockCipher.iv } returns ByteArray(12)
        every { mockCipher.doFinal(any<ByteArray>()) } throws RuntimeException("cipher error")

        val result = cryptoManager.encrypt("test")

        assertNull(result)
        verify { Log.d(TAG, "Failed to encrypt: cipher error") }
    }

    // ── decrypt ─────────────────────────────────────────────────────────

    @Test
    fun `given valid encoded string, when decrypt is called, then splits IV and ciphertext and decrypts correctly`() {
        val fakeIv = ByteArray(CryptoManager.GCM_IV_LENGTH) { 1 }
        val fakeCipherText = ByteArray(20) { 2 }
        val combined = fakeIv + fakeCipherText
        val specSlot = slot<GCMParameterSpec>()
        val cipherTextSlot = slot<ByteArray>()

        every { Base64.decode("encoded", Base64.NO_WRAP) } returns combined
        every { mockCipher.init(Cipher.DECRYPT_MODE, mockKey, capture(specSlot)) } just Runs
        every { mockCipher.doFinal(capture(cipherTextSlot)) } returns "decrypted".toByteArray(Charsets.UTF_8)

        val result = cryptoManager.decrypt("encoded")

        assertEquals("decrypted", result)
        verify { Cipher.getInstance(CryptoManager.TRANSFORMATION) }
        assertEquals(CryptoManager.GCM_TAG_LENGTH, specSlot.captured.tLen)
        assertArrayEquals(fakeIv, specSlot.captured.iv)
        assertArrayEquals(fakeCipherText, cipherTextSlot.captured)
        verify(exactly = 0) { Log.d(any(), any()) }
    }

    @Test
    fun `given Base64 decode throws, when decrypt is called, then returns null and logs error`() {
        every { Base64.decode("bad", Base64.NO_WRAP) } throws IllegalArgumentException("bad base64")

        val result = cryptoManager.decrypt("bad")

        assertNull(result)
        verify { Log.d(TAG, "Failed to decrypt: bad base64") }
    }

    @Test
    fun `given keyProvider throws, when decrypt is called, then returns null and logs error`() {
        val fakeIv = ByteArray(CryptoManager.GCM_IV_LENGTH) { 1 }
        val combined = fakeIv + ByteArray(20) { 2 }

        every { Base64.decode("encoded", Base64.NO_WRAP) } returns combined
        every { keyProvider.getKey() } throws RuntimeException("no key")

        val result = cryptoManager.decrypt("encoded")

        assertNull(result)
        verify { Log.d(TAG, "Failed to decrypt: no key") }
    }

    @Test
    fun `given cipher doFinal throws, when decrypt is called, then returns null and logs error`() {
        val fakeIv = ByteArray(CryptoManager.GCM_IV_LENGTH) { 1 }
        val combined = fakeIv + ByteArray(20) { 2 }

        every { Base64.decode("encoded", Base64.NO_WRAP) } returns combined
        every { mockCipher.init(Cipher.DECRYPT_MODE, mockKey, any<GCMParameterSpec>()) } just Runs
        every { mockCipher.doFinal(any<ByteArray>()) } throws RuntimeException("tampered")

        val result = cryptoManager.decrypt("encoded")

        assertNull(result)
        verify { Log.d(TAG, "Failed to decrypt: tampered") }
    }
}