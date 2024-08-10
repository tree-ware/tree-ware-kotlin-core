package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test

class CopyTests {
    @Test
    fun `Copy operator must copy all elements of its input`() {
        val modelJsonFile = "model/address_book_1.json"
        val input = AddressBookMutableEntityModelFactory.create()
        decodeJsonFileIntoEntity(modelJsonFile, entity = input)
        val clone = AddressBookMutableEntityModelFactory.create()
        copy(input, clone)
        assertMatchesJson(clone, modelJsonFile, EncodePasswords.ALL)
    }
}