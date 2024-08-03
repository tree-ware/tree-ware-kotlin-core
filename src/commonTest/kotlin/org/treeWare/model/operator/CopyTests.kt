package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonFile
import kotlin.test.Test

class CopyTests {
    @Test
    fun `Copy operator must copy all elements of its input`() {
        val modelJsonFile = "model/address_book_1_main_model.json"
        val input = getMainModelFromJsonFile(addressBookMetaModel, modelJsonFile)
        val clone = MutableMainModel(addressBookMetaModel)
        copy(input, clone)
        assertMatchesJson(clone, modelJsonFile, EncodePasswords.ALL)
    }
}