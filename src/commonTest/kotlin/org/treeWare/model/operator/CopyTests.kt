package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookRootEntityMeta
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test

class CopyTests {
    @Test
    fun `Copy operator must copy all elements of its input`() {
        val modelJsonFile = "model/address_book_1.json"
        val input = MutableEntityModel(addressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(modelJsonFile, entity = input)
        val clone = MutableEntityModel(addressBookRootEntityMeta, null)
        copy(input, clone)
        assertMatchesJson(clone, modelJsonFile, EncodePasswords.ALL)
    }
}