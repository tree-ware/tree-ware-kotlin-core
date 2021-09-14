package org.treeWare.model.core

interface Cipher {
    val cipherVersion: Int
    fun encrypt(decrypted: String): String
    fun decrypt(encrypted: String): String
}
