package org.treeWare.model.core

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CipherV1(private val encryptionPassword: String) : org.treeWare.model.core.Cipher {
    companion object {
        const val secretKeyFactoryName = "PBKDF2WithHmacSHA1"
        const val keySpecIterationCount = 10000
        const val keyLength = 256

        const val cipherName = "AES/CBC/PKCS5Padding"
        const val saltSize = 16
        const val ivSize = 16
    }

    override val encryptionVersion = 1

    override fun encrypt(decrypted: String): String {
        val salt = newRandomBytes(saltSize)
        val secretKey = newSecretKey(encryptionPassword, salt)
        val iv = newRandomBytes(ivSize)
        val cipher = newCipher(Cipher.ENCRYPT_MODE, secretKey, iv)
        val encrypted = cipher.doFinal(decrypted.toByteArray(Charsets.UTF_8))
        // Prepend salt and IV (since they are random and needed for decryption).
        val combined = combine(salt, iv, encrypted)
        return Base64.getEncoder().encodeToString(combined)
    }

    override fun decrypt(encrypted: String): String {
        val combined = Base64.getDecoder().decode(encrypted)
        val (salt, iv, encryptedActual) = split(combined)
        val secretKey = newSecretKey(encryptionPassword, salt)
        val cipher = newCipher(Cipher.DECRYPT_MODE, secretKey, iv)
        val decrypted = cipher.doFinal(encryptedActual)
        return String(decrypted, Charsets.UTF_8)
    }

    private fun newRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun newSecretKey(encryptionPassword: String, salt: ByteArray): SecretKeySpec {
        val keyFactory = SecretKeyFactory.getInstance(secretKeyFactoryName)
        val keySpec = PBEKeySpec(encryptionPassword.toCharArray(), salt, keySpecIterationCount, keyLength)
        val secret = keyFactory.generateSecret(keySpec)
        return SecretKeySpec(secret.encoded, "AES")
    }

    private fun newCipher(mode: Int, secretKey: SecretKeySpec, iv: ByteArray): Cipher {
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance(cipherName)
        cipher.init(mode, secretKey, ivSpec)
        return cipher
    }

    private fun combine(salt: ByteArray, iv: ByteArray, encrypted: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(salt.size + iv.size + encrypted.size)
        buffer.put(salt)
        buffer.put(iv)
        buffer.put(encrypted)
        return buffer.array()
    }

    private fun split(saltIvEncrypted: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val buffer = ByteBuffer.wrap(saltIvEncrypted)
        val salt = ByteArray(saltSize)
        val iv = ByteArray(ivSize)
        val encrypted = ByteArray(saltIvEncrypted.size - saltSize - ivSize)
        buffer.get(salt, 0, salt.size)
        buffer.get(iv, 0, iv.size)
        buffer.get(encrypted, 0, encrypted.size)
        return Triple(salt, iv, encrypted)
    }
}
