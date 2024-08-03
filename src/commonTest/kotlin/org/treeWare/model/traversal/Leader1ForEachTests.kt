package org.treeWare.model.traversal

import okio.Buffer
import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.newAddressBook
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class Leader1ForEachTests {
    @Test
    fun `Leader1ForEach must visit and leave all elements of a constructed model`() {
        val addressBook = newAddressBook("aux")

        val buffer = Buffer()
        val printVisitor = Leader1PrintVisitor(buffer)
        forEach(addressBook, printVisitor, true)

        val expected = readFile("model/traversal/address_book_print.txt")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }

    @Test
    fun `Leader1ForEach must visit and leave all elements of a decoded model`() {
        val addressBook = getMainModelFromJsonFile(addressBookMetaModel, "model/address_book_1_main_model.json")

        val buffer = Buffer()
        val printVisitor = Leader1PrintVisitor(buffer)
        forEach(addressBook, printVisitor, true)

        val expected = readFile("model/traversal/address_book_print_1.txt")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }
}