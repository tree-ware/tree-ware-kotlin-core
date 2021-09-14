package org.treeWare.model.core

import org.treeWare.metaModel.ADDRESS_BOOK_META_MODEL_FILE_PATH
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test

class AddressBookMetaModelTests {
    @Test
    fun `Address-book meta-model JSON codec round trip must be lossless`() {
        val metaModel = newAddressBookMetaModel(null, null)
        assertMatchesJson(metaModel, null, ADDRESS_BOOK_META_MODEL_FILE_PATH, EncodePasswords.NONE)
    }
}
