package org.treeWare.model.traversal

import kotlinx.coroutines.runBlocking
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.readFile
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LeaderManyForEachTests {
    @Test
    fun `LeaderManyForEach must visit and leave all elements of all leaders`() = runBlocking {
        // Ensure inputs are different so that the test is not trivial.
        val jsonAddressBook2 = readFile("model/address_book_2.json")
        val jsonAddressBook3 = readFile("model/address_book_3.json")
        assertNotEquals(jsonAddressBook2, jsonAddressBook3)

        val metaModel = newAddressBookMetaModel(null, null)
        val addressBook2 = getMainModelFromJsonString(metaModel, jsonAddressBook2)
        val addressBook3 = getMainModelFromJsonString(metaModel, jsonAddressBook3)

        val writer = StringWriter()
        val printVisitor = LeaderManyPrintVisitor(writer)
        forEach(listOf(addressBook2, addressBook3), printVisitor)

        val expected = readFile("model/traversal/address_book_print_2_3.txt")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }
}
