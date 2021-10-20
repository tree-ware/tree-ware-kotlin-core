package org.treeWare.model.core

import org.treeWare.metaModel.ADDRESS_BOOK_META_MODEL_FILES
import org.treeWare.metaModel.newMainMetaMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonFile
import kotlin.test.Test

class AddressBookMetaModelTests {
    @Test
    fun `Address-book meta-model JSON codec round trip must be lossless`() {
        val metaMetaModel = newMainMetaMetaModel()
        ADDRESS_BOOK_META_MODEL_FILES.forEach { file ->
            println("Testing file: $file...")
            val metaModel = getMainModelFromJsonFile(metaMetaModel, file)
            assertMatchesJson(metaModel, null, file, EncodePasswords.NONE)
        }
    }
}
