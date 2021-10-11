package org.treeWare.model.operator

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonFile
import kotlin.test.Test

class CopyTests {
    @Test
    fun `Copy operator must copy all elements of its input`() {
        val modelJsonFile = "model/address_book_1.json"
        val metaModel = newAddressBookMetaModel(null, null)
        val input = getMainModelFromJsonFile<Unit>(metaModel, modelJsonFile)
        val clone = MutableMainModel<Unit>(metaModel)
        copy(input, clone)
        assertMatchesJson(clone, null, modelJsonFile, EncodePasswords.ALL)
    }
}
