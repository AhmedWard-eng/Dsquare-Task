package com.dsquares.library.security

import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CryptoManagerTest {

    private lateinit var testKey: SecretKey
    private lateinit var keyStoreProvider: KeyStoreProvider
    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setup() {
        // Generate a real AES-256 key in memory (no Android Keystore needed)
        testKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        keyStoreProvider = mockk<KeyStoreProvider>()
        every { keyStoreProvider.getKey() } returns testKey
        cryptoManager = CryptoManager(keyStoreProvider)

        // Mock android.util.Base64 to delegate to java.util.Base64
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any<ByteArray>(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    // ── encrypt tests ─────────────────────────────────────────────────────

    @Test
    fun `given plain text, when encrypt is called, then returns non-null Base64 string`() {
        val result = cryptoManager.encrypt("hello world")
        assertNotNull(result)
    }

    @Test
    fun `given empty string, when encrypt is called, then returns non-null result`() {
        val result = cryptoManager.encrypt("")
        assertNotNull(result)
    }

    @Test
    fun `given same plain text, when encrypt is called twice, then results differ due to random IV`() {
        val first = cryptoManager.encrypt("same input")
        val second = cryptoManager.encrypt("same input")
        assertNotEquals(first, second)
    }

    @Test
    fun `given key provider throws, when encrypt is called, then returns null`() {
        val brokenProvider = mockk<KeyStoreProvider>()
        every { brokenProvider.getKey() } throws RuntimeException("no key")
        val brokenManager = CryptoManager(brokenProvider)
        assertNull(brokenManager.encrypt("test"))
    }

    // ── decrypt tests ─────────────────────────────────────────────────────

    @Test
    fun `given invalid Base64, when decrypt is called, then returns null`() {
        assertNull(cryptoManager.decrypt("not-valid-base64!!!"))
    }

    @Test
    fun `given too short ciphertext, when decrypt is called, then returns null`() {
        // Less than 12 bytes (GCM_IV_LENGTH) after decoding
        val shortData = java.util.Base64.getEncoder().encodeToString(ByteArray(5))
        assertNull(cryptoManager.decrypt(shortData))
    }

    @Test
    fun `given tampered ciphertext, when decrypt is called, then returns null`() {
        val encrypted = cryptoManager.encrypt("secret")!!
        val bytes = java.util.Base64.getDecoder().decode(encrypted)
        // Flip a byte in the ciphertext portion (after the 12-byte IV)
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 0xFF).toByte()
        val tampered = java.util.Base64.getEncoder().encodeToString(bytes)
        assertNull(cryptoManager.decrypt(tampered))
    }

    @Test
    fun `given key provider throws, when decrypt is called, then returns null`() {
        val encrypted = cryptoManager.encrypt("test")!!
        val brokenProvider = mockk<KeyStoreProvider>()
        every { brokenProvider.getKey() } throws RuntimeException("no key")
        val brokenManager = CryptoManager(brokenProvider)
        assertNull(brokenManager.decrypt(encrypted))
    }

    @Test
    fun `given ciphertext from different key, when decrypt is called, then returns null`() {
        val encrypted = cryptoManager.encrypt("secret")!!
        val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val otherProvider = mockk<KeyStoreProvider>()
        every { otherProvider.getKey() } returns otherKey
        val otherManager = CryptoManager(otherProvider)
        assertNull(otherManager.decrypt(encrypted))
    }

    // ── encrypt → decrypt round-trip tests ────────────────────────────────

    @Test
    fun `given plain text, when encrypted then decrypted, then original text is recovered`() {
        val original = "hello world"
        val encrypted = cryptoManager.encrypt(original)
        val decrypted = cryptoManager.decrypt(encrypted!!)
        assertEquals(original, decrypted)
    }

    @Test
    fun `given empty string, when encrypted then decrypted, then empty string is recovered`() {
        val encrypted = cryptoManager.encrypt("")
        val decrypted = cryptoManager.decrypt(encrypted!!)
        assertEquals("", decrypted)
    }

    @Test
    fun `given unicode text, when encrypted then decrypted, then original text is recovered`() {
        val original = "مرحبا بالعالم 🌍"
        val encrypted = cryptoManager.encrypt(original)
        val decrypted = cryptoManager.decrypt(encrypted!!)
        assertEquals(original, decrypted)
    }

    @Test
    fun `given long text, when encrypted then decrypted, then original text is recovered`() {
        val original = "a".repeat(10_000)
        val encrypted = cryptoManager.encrypt(original)
        val decrypted = cryptoManager.decrypt(encrypted!!)
        assertEquals(original, decrypted)
    }

    @Test
    fun `given special characters, when encrypted then decrypted, then original text is recovered`() {
        val original = "line1\nline2\ttab\r\n\"quotes\" & <symbols>"
        val encrypted = cryptoManager.encrypt(original)
        val decrypted = cryptoManager.decrypt(encrypted!!)
        assertEquals(original, decrypted)
    }

    // ── output format tests ───────────────────────────────────────────────

    @Test
    fun `given encrypted output, when decoded, then first 12 bytes are IV and rest is ciphertext plus tag`() {
        val encrypted = cryptoManager.encrypt("test")!!
        val combined = java.util.Base64.getDecoder().decode(encrypted)
        // IV (12 bytes) + ciphertext + GCM tag (16 bytes)
        // "test" is 4 bytes, so minimum expected size = 12 + 4 + 16 = 32
        assert(combined.size >= CryptoManager.GCM_IV_LENGTH + 4 + (CryptoManager.GCM_TAG_LENGTH / 8))
    }
}