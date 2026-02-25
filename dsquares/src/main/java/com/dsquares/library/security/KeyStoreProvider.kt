package com.dsquares.library.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

// Provides an AES-256 key from the Android Keystore, creating one if it doesn't exist.
// The key is cached after the first access to avoid repeated Keystore lookups.
internal class KeyStoreProvider {

    companion object {
        private const val KEYSTORE_ALIAS = "dsquare_token_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    @Volatile
    private var cachedKey: SecretKey? = null

    fun getKey(): SecretKey {
        cachedKey?.let { return it }

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val key = keyStore.getEntry(KEYSTORE_ALIAS, null)?.let { entry ->
            (entry as KeyStore.SecretKeyEntry).secretKey
        } ?: createKey()

        cachedKey = key
        return key
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }
}