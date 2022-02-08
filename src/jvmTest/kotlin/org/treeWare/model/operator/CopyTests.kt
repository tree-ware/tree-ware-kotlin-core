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
        val input = getMainModelFromJsonFile(metaModel, modelJsonFile)
        val clone = MutableMainModel(metaModel)
        copy(input, clone)
        assertMatchesJson(clone, modelJsonFile, EncodePasswords.ALL)
    }
}