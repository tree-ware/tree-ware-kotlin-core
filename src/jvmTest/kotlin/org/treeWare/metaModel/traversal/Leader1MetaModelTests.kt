package org.treeWare.metaModel.traversal

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.readFile
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class Leader1MetaModelTests {
    @Test
    fun `Traverse address-book meta-model with adapter`() {
        val metaModel = newAddressBookMetaModel(null, null).metaModel
            ?: throw IllegalStateException("Meta-model has validation errors")

        val writer = StringWriter()
        val printVisitor = Leader1MetaModelPrintVisitor(writer)
        metaModelForEach(metaModel, printVisitor)

        val expected = readFile("metaModel/traversal/address_book_meta_model_print.json")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }
}
