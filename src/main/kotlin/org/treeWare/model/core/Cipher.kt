package org.treeWare.model.core

interface Cipher {
    val encryptionVersion: Int
    fun encrypt(decrypted: String): String
    fun decrypt(encrypted: String): String
}
