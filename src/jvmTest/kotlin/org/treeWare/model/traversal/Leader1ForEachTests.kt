package org.treeWare.model.traversal

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.newAddressBook
import org.treeWare.util.readFile
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class Leader1ForEachTests {
    @Test
    fun `Leader1ForEach must visit and leave all elements of a constructed model`() {
        val addressBook = newAddressBook("aux")

        val writer = StringWriter()
        val printVisitor = Leader1PrintVisitor(writer)
        forEach(addressBook, printVisitor, true)

        val expected = readFile("model/traversal/address_book_print.txt")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun `Leader1ForEach must visit and leave all elements of a decoded model`() {
        val addressBook = getMainModelFromJsonFile(addressBookMetaModel, "model/address_book_1.json")

        val writer = StringWriter()
        val printVisitor = Leader1PrintVisitor(writer)
        forEach(addressBook, printVisitor, true)

        val expected = readFile("model/traversal/address_book_print_1.txt")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }
}