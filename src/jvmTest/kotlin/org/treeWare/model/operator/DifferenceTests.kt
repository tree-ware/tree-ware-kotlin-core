package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
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
        val expectedUpdateTestOutput = "model/operator/difference/mini_test_book_difference_update_1_2.json"
        assertNotEquals(jsonInput1, jsonInput2)

        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val input2 = getMainModelFromJsonString(addressBookMetaModel, jsonInput2)
        val output = difference(input1, input2)
        val createOutput = output.createModel
        val deleteOutput = output.deleteModel
        val updateOutput = output.updateModel

        assertNotNull(createOutput)
        assertMatchesJson(createOutput, expectedCreateTestOutput, EncodePasswords.ALL)

        assertNull(deleteOutput)

        assertNotNull(updateOutput)
        assertMatchesJson(updateOutput, expectedUpdateTestOutput, EncodePasswords.ALL)
    }

    @Test
    fun `Union of old with create and update models must result in the new model`() {
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")
        val jsonInput2 = readFile("model/operator/difference/mini_test_book_2.json")
        val expectedMergeTestResult = "model/operator/difference/mini_test_book_2_reordered.json"

        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val input2 = getMainModelFromJsonString(addressBookMetaModel, jsonInput2)
        val output = difference(input1, input2)
        val createOutput = output.createModel
        val updateOutput = output.updateModel

        assertNotNull(createOutput)
        assertNotNull(updateOutput)

        val mergeCreateOutput = union(listOf(input1, createOutput))
        val mergeUpdateOutput = union(listOf(mergeCreateOutput, updateOutput))
        assertMatchesJson(
            mergeUpdateOutput, expectedMergeTestResult, EncodePasswords.ALL, MultiAuxEncoder()
        )
    }

    @Test
    fun `Switching difference operator inputs must produce flipped add and delete outputs`() {
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
    fun `Difference operator on identical inputs must return all null outputs`() {
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")

        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val output = difference(input1, input1)
        val createOutput = output.createModel
        val deleteOutput = output.deleteModel
        val updateOutput = output.updateModel

        assertNull(createOutput)
        assertNull(deleteOutput)
        assertNull(updateOutput)

    }

    @Test
    fun `Difference must be able to produce three correct non-null models`() {
        // Ensure inputs are all different so that the test is not trivial.
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_2.json")
        val jsonInput2 = readFile("model/operator/difference/mini_test_book_3.json")
        val expectedCreateTestOutput = "model/operator/difference/mini_test_book_difference_create_2_3.json"
        val expectedDeleteTestOutput = "model/operator/difference/mini_test_book_difference_delete_2_3.json"
        val expectedUpdateTestOutput = "model/operator/difference/mini_test_book_difference_update_2_3.json"
        assertNotEquals(jsonInput1, jsonInput2)

        val input1 = getMainModelFromJsonString(addressBookMetaModel, jsonInput1)
        val input2 = getMainModelFromJsonString(addressBookMetaModel, jsonInput2)
        val output = difference(input1, input2)
        val createOutput = output.createModel
        val deleteOutput = output.deleteModel
        val updateOutput = output.updateModel

        assertNotNull(createOutput)
        assertMatchesJson(createOutput, expectedCreateTestOutput, EncodePasswords.ALL)

        assertNotNull(deleteOutput)
        assertMatchesJson(deleteOutput, expectedDeleteTestOutput, EncodePasswords.ALL)

        assertNotNull(updateOutput)
        assertMatchesJson(updateOutput, expectedUpdateTestOutput, EncodePasswords.ALL)
    }


}
