package org.treeWare.model

import org.treeWare.model.core.Cipher

private const val PREFIX = "test-encrypted-"

class TestCipherV1 : Cipher {
    override val encryptionVersion: Int = 1

    override fun encrypt(decrypted: String): String = "$PREFIX-$decrypted"

    override fun decrypt(encrypted: String): String =
        if (encrypted.startsWith(PREFIX)) encrypted.substring(PREFIX.length) else ""
}
