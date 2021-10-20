package org.treeWare.model.operator

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.readFile
import kotlin.test.Test
import kotlin.test.assertNotEquals

class UnionTests {
    @Test
    fun `Union operator must return union of its inputs`() {
        // Ensure inputs and output are all different so that the test is not trivial.
        val expectedOutputJsonFile = "model/operator/address_book_union_2_3.json"
        val jsonInput1 = readFile("model/address_book_2.json")
        val jsonInput2 = readFile("model/address_book_3.json")
        val expectedJsonOutput = readFile(expectedOutputJsonFile)
        assertNotEquals(jsonInput1, jsonInput2)
        assertNotEquals(jsonInput1, expectedJsonOutput)
        assertNotEquals(jsonInput2, expectedJsonOutput)

        val metaModel = newAddressBookMetaModel(null, null)
        val input1 = getMainModelFromJsonString(metaModel, jsonInput1)
        val input2 = getMainModelFromJsonString(metaModel, jsonInput2)
        val output = union(listOf(input1, input2))
        assertMatchesJson(output, expectedOutputJsonFile, EncodePasswords.ALL)
    }
}
