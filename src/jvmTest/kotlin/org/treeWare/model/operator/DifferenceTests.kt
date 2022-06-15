package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.readFile
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DifferenceTests {
    @Test
    fun `Difference operator must return difference of its inputs`() {
        // Ensure inputs are all different so that the test is not trivial.
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")
        val jsonInput2 = readFile("model/operator/difference/mini_test_book_2.json")
        val expectedCreateTestOutput = "model/operator/difference/mini_test_book_difference_create_1_2.json"
        assertNotEquals(jsonInput1, jsonInput2)

        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val input2 = getMainModelFromJsonString(addressBookMetaModel, jsonInput2)
        val output = difference(input1, input2)
        val addOutput = output.createModel
        val deleteOutput = output.deleteModel

        assertNotNull(addOutput)
        assertMatchesJson(addOutput, expectedCreateTestOutput, EncodePasswords.ALL)

        assertNull(deleteOutput)
    }

    @Test
    fun `Switching difference operator inputs produces flipped add and delete outputs`() {
        // Ensure inputs are all different so that the test is not trivial.
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")
        val jsonInput2 = readFile("model/operator/difference/mini_test_book_2.json")
        val expectedCreateTestOutput = "model/operator/difference/mini_test_book_difference_create_1_2.json"
        assertNotEquals(jsonInput1, jsonInput2)


        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val input2 = getMainModelFromJsonString(addressBookMetaModel, jsonInput2)
        val output = difference(input2, input1)
        val createOutput = output.createModel
        val deleteOutput = output.deleteModel

        assertNotNull(deleteOutput)
        assertMatchesJson(deleteOutput, expectedCreateTestOutput, EncodePasswords.ALL)

        assertNull(createOutput)
    }

    @Test
    fun `Difference operator on identical inputs should return all null outputs`() {
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")

        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val output = difference(input1, input1)
        val createOutput = output.createModel
        val deleteOutput = output.deleteModel

        assertNull(createOutput)
        assertNull(deleteOutput)
    }
}
