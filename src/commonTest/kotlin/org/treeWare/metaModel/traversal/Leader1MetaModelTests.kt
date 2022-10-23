package org.treeWare.metaModel.traversal

import okio.Buffer
import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class Leader1MetaModelTests {
    @Test
    fun `Traverse address-book meta-model with adapter`() {
        val buffer = Buffer()
        val printVisitor = Leader1MetaModelPrintVisitor(buffer)
        metaModelForEach(addressBookMetaModel, printVisitor)

        val expected = readFile("metaModel/traversal/address_book_meta_model_print.json")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }
}