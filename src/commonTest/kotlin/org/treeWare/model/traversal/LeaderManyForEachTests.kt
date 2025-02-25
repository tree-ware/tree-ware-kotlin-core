package org.treeWare.model.traversal

import okio.Buffer
import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LeaderManyForEachTests {
    @Test
    fun `LeaderManyForEach must visit and leave all elements of all leaders`() {
        // Ensure inputs are different so that the test is not trivial.
        val jsonAddressBook2 = readFile("model/address_book_2.json")
        val jsonAddressBook3 = readFile("model/address_book_3.json")
        assertNotEquals(jsonAddressBook2, jsonAddressBook3)

        val addressBook2 = addressBookRootEntityFactory(null)
        decodeJsonStringIntoEntity(jsonAddressBook2, entity = addressBook2)
        val addressBook3 = addressBookRootEntityFactory(null)
        decodeJsonStringIntoEntity(jsonAddressBook3, entity = addressBook3)

        val buffer = Buffer()
        val printVisitor = LeaderManyPrintVisitor(buffer)
        forEach(listOf(addressBook2, addressBook3), printVisitor, true)

        val expected = readFile("model/traversal/address_book_print_2_3.txt")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }
}