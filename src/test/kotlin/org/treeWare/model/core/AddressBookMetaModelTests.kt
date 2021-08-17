package org.treeWare.model.core

import org.treeWare.model.assertMatchesJson
import org.treeWare.schema.core.newAddressBookMetaModel
import kotlin.test.Test

val addressBookMetaModelFilePath = "model/address_book_meta_model.json"

class AddressBookMetaModelTests {
    // TODO(self-hosting): uncomment test after meta-meta-model is defined.
//    @Test
//    fun `Address-book meta-model JSON codec round trip must be lossless`() {
//        val errors = validate(metaModelSchema)
//        assertTrue(errors.isEmpty())
//
//
//        val metaModel = getModel<Unit>(metaModelSchema, addressBookMetaModelFilePath, "data") { null }
//        assertMatchesJson(metaModel, null, addressBookMetaModelFilePath)
//    }

    @Test
    fun `Address-book Kotlin meta-model must match JSON meta-model`() {
        val metaModel = newAddressBookMetaModel()
        assertMatchesJson(metaModel, null, addressBookMetaModelFilePath)
    }
}
