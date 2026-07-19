package com.example.data.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {
    private const val ALGORITHM = "AES"
    // 16-byte key for AES-128
    private val KEY = "SmartContactIdKey".toByteArray(Charsets.UTF_8)

    fun encrypt(value: String): String {
        if (value.isEmpty()) return ""
        return try {
            val keySpec = SecretKeySpec(KEY, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            value // fallback
        }
    }

    fun decrypt(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return try {
            val keySpec = SecretKeySpec(KEY, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(value, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            value // fallback
        }
    }
}
