package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test

class CopyTests {
    @Test
    fun `Copy operator must copy all elements of its input`() {
        val modelJsonFile = "model/address_book_1.json"
        val input = addressBookRootEntityFactory(null)
        decodeJsonFileIntoEntity(modelJsonFile, entity = input)
        val clone = addressBookRootEntityFactory(null)
        copy(input, clone)
        assertMatchesJson(clone, modelJsonFile, EncodePasswords.ALL)
    }
}