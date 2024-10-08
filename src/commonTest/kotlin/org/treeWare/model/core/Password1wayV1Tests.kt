package org.treeWare.model.core

import kotlin.test.*

private const val UNHASHED = "password123"
private const val UNHASHED_INCORRECT = "incorrectPassword"

private val HASHER_CLIENT = null
private val HASHER_SERVER = HasherV1()

/** Hashed password corresponding to the above UNHASHED password. */
private const val HASHED = "\$2a\$10\$u1r09AnODCkNaUh2mkP8bubLWZ2Wusu94BpJcYGUpCDlOXoJs05D6"
private const val HASH_VERSION = 1

class Password1wayV1Tests {
    @Test
    fun `Setting unhashed in client library must set unhashed`() {
        val password1way = getPassword1wayModel(HASHER_CLIENT)
        password1way.setUnhashed(UNHASHED)
        assertEquals(UNHASHED, password1way.unhashed)
        assertNull(password1way.hashed)
        assertEquals(0, password1way.hashVersion)
    }

    @Test
    fun `Setting unhashed in server library must set hashed`() {
        val password1way = getPassword1wayModel(HASHER_SERVER)
        password1way.setUnhashed(UNHASHED)
        assertNull(password1way.unhashed)
        assertNotNull(password1way.hashed)
        assertNotEquals(UNHASHED, password1way.hashed)
        assertEquals(HASH_VERSION, password1way.hashVersion)
    }

    @Test
    fun `Setting hashed must set hashed and version`() {
        val password1way = getPassword1wayModel(HASHER_CLIENT)
        password1way.setHashed(HASHED, HASH_VERSION)
        assertNull(password1way.unhashed)
        assertEquals(HASHED, password1way.hashed)
        assertEquals(HASH_VERSION, password1way.hashVersion)
    }

    @Test
    fun `Verifying unhashed against unhashed in client library must return false`() {
        val password1way = getPassword1wayModel(HASHER_CLIENT)
        password1way.setUnhashed(UNHASHED)
        assertFalse(password1way.verify(UNHASHED))
    }

    @Test
    fun `Verifying unhashed against hashed in client library must return false`() {
        val password1way = getPassword1wayModel(HASHER_CLIENT)
        password1way.setHashed(HASHED, HASH_VERSION)
        assertFalse(password1way.verify(UNHASHED))
    }

    @Test
    fun `Verifying non-matching unhashed in server library must return false`() {
        val password1way = getPassword1wayModel(HASHER_SERVER)
        password1way.setHashed(HASHED, HASH_VERSION)
        assertFalse(password1way.verify(UNHASHED_INCORRECT))
    }

    @Test
    fun `Verifying matching unhashed in server library must return true`() {
        val password1way = getPassword1wayModel(HASHER_SERVER)
        password1way.setHashed(HASHED, HASH_VERSION)
        assertTrue(password1way.verify(UNHASHED))
    }
}

private fun getPassword1wayModel(hasher: HasherV1?): MutablePassword1wayModel {
    val resolved = Resolved("dummy/field")
    resolved.password1wayHasher = hasher

    val dummyRootMeta = MutableEntityModel(null, null)
    val dummyFieldParentMeta = MutableSingleFieldModel(null, dummyRootMeta, MutablePrimitiveModel.fieldValueFactory)
    val dummyFieldMeta = MutableEntityModel(null, dummyFieldParentMeta)
    dummyFieldMeta.setAux(RESOLVED_AUX, resolved)

    val dummyEntity = MutableEntityModel(null, null)
    val dummyField = MutableSingleFieldModel(dummyFieldMeta, dummyEntity, MutablePrimitiveModel.fieldValueFactory)
    return MutablePassword1wayModel(dummyField)
}