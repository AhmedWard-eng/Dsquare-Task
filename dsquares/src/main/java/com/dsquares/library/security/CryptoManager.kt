package com.dsquares.library.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

// Encrypts/decrypts strings using AES/GCM with a key from the provided source.
class CryptoManager internal constructor(
    private val keyProvider: KeyStoreProvider = KeyStoreProvider()
) {

    companion object {
        // AES encryption + GCM (provides integrity check) + no padding needed for GCM
        internal const val TRANSFORMATION = "AES/GCM/NoPadding"
        // Authentication tag size in bits — verifies data wasn't tampered with
        internal const val GCM_TAG_LENGTH = 128
        // IV size in bytes — ensures each encryption produces unique output
        internal const val GCM_IV_LENGTH = 12
    }

    fun encrypt(plainText: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            // GCM auto-generates a random 12-byte IV
            cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getKey())
            val iv = cipher.iv
            // doFinal returns: encrypted data + 16-byte authentication tag appended at the end
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            // Combine IV + encrypted data into one array for storage
            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    fun decrypt(encryptedText: String): String? {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            // Split: first 12 bytes = IV, rest = ciphertext + tag
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val cipherText = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            // Must provide the same IV and tag length used during encryption
            cipher.init(
                Cipher.DECRYPT_MODE,
                keyProvider.getKey(),
                GCMParameterSpec(GCM_TAG_LENGTH, iv)
            )
            // Decrypts and verifies integrity — throws exception if data was tampered with
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}