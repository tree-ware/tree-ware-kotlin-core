package org.treeWare.metaModel.traversal

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.readFile
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class Leader1Follower0MetaModelTests {
    @Test
    fun `Traverse address-book meta-model with adapter`() {
        val metaModel = newAddressBookMetaModel(null, null)

        val writer = StringWriter()
        val printVisitor = Leader1Follower0MetaModelPrintVisitor(writer)
        metaModelForEach(metaModel, printVisitor)

        val expected = readFile("metaModel/traversal/address_book_meta_model_print.json")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }
}
