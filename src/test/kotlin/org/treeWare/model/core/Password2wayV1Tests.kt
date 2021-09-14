package org.treeWare.model.core

import javax.crypto.Cipher
import kotlin.test.*

private const val UNENCRYPTED = "password123"
private const val ENCRYPTED = "jjtphq6AZjMo1S824le3Xh6yVHhcWG29CfYH7J8tmcAhD5NhVoQbuqRtzJgWn1H4"

private val CIPHER_CLIENT = null
private val CIPHER_SERVER = CipherV1("secretKey123")

private const val ENCRYPTION_VERSION = 1

class Password2wayV1Tests {
    @Test
    fun `Cipher getInstance() must not not return a shared instance`() {
        val cipher1 = Cipher.getInstance(CipherV1.cipherName)
        val cipher2 = Cipher.getInstance(CipherV1.cipherName)
        assertNotEquals(cipher1, cipher2)
    }

    @Test
    fun `Setting unencrypted in client library must set unencrypted`() {
        val password2way = getPassword2wayModel(CIPHER_CLIENT)
        password2way.setUnencrypted(UNENCRYPTED)
        assertEquals(UNENCRYPTED, password2way.unencrypted)
        assertNull(password2way.encrypted)
        assertEquals(0, password2way.encryptionVersion)
    }

    @Test
    fun `Setting unencrypted in server library must set encrypted`() {
        val password2way = getPassword2wayModel(CIPHER_SERVER)
        password2way.setUnencrypted(UNENCRYPTED)
        assertNull(password2way.unencrypted)
        assertNotNull(password2way.encrypted)
        assertNotEquals(UNENCRYPTED, password2way.encrypted)
        assertEquals(ENCRYPTION_VERSION, password2way.encryptionVersion)
    }

    @Test
    fun `Setting encrypted must set encrypted and version`() {
        val password2way = getPassword2wayModel(CIPHER_CLIENT)
        password2way.setEncrypted(ENCRYPTED, ENCRYPTION_VERSION)
        assertNull(password2way.unencrypted)
        assertEquals(ENCRYPTED, password2way.encrypted)
        assertEquals(ENCRYPTION_VERSION, password2way.encryptionVersion)
    }

    @Test
    fun `Decrypting without unencrypted in client library must return null`() {
        val password2way = getPassword2wayModel(CIPHER_CLIENT)
        assertNull(password2way.decrypt())
    }

    @Test
    fun `Decrypting with unencrypted in client library must return unencrypted`() {
        val password2way = getPassword2wayModel(CIPHER_CLIENT)
        password2way.setUnencrypted(UNENCRYPTED)
        assertEquals(UNENCRYPTED, password2way.decrypt())
    }

    @Test
    fun `Decrypting with encrypted in client library must return null`() {
        val password2way = getPassword2wayModel(CIPHER_CLIENT)
        password2way.setEncrypted(ENCRYPTED, ENCRYPTION_VERSION)
        assertNull(password2way.decrypt())
    }

    @Test
    fun `Decrypting after setting unencrypted in server library must return decrypted`() {
        val password2way = getPassword2wayModel(CIPHER_SERVER)
        password2way.setUnencrypted(UNENCRYPTED)
        assertEquals(UNENCRYPTED, password2way.decrypt())
    }

    @Test
    fun `Decrypting after setting encrypted in server library must return decrypted`() {
        val password2way = getPassword2wayModel(CIPHER_SERVER)
        password2way.setEncrypted(ENCRYPTED, ENCRYPTION_VERSION)
        assertEquals(UNENCRYPTED, password2way.decrypt())
    }
}

private fun getPassword2wayModel(cipher: CipherV1?): MutablePassword2wayModel<Unit> {
    val resolved = Resolved("dummy/field")
    resolved.password2wayCipher = cipher

    val dummyMainMeta = MutableMainModel<Resolved>(null)
    val dummyRootMeta = MutableRootModel(null, dummyMainMeta)
    val dummyFieldParentMeta = MutableSingleFieldModel(null, dummyRootMeta)
    val dummyFieldMeta = MutableEntityModel(null, dummyFieldParentMeta)
    dummyFieldMeta.aux = resolved

    val dummyMain = MutableMainModel<Unit>(null)
    val dummyEntity = MutableRootModel(null, dummyMain)
    val dummyField = MutableSingleFieldModel(dummyFieldMeta, dummyEntity)
    return MutablePassword2wayModel(dummyField)
}
