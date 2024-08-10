package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.StringAuxEncoder
import org.treeWare.util.readFile
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

        val aux2 = "aux2"
        val aux3 = "aux3"
        val multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
            aux2 to { StringAuxStateMachine(it) },
            aux3 to { StringAuxStateMachine(it) }
        )

        val input1 = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            jsonInput1,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingStateMachineFactory,
            entity = input1
        )
        val input2 = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            jsonInput2,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingStateMachineFactory,
            entity = input2
        )
        val output = AddressBookMutableEntityModelFactory.create()
        union(listOf(input1, input2), output)
        assertMatchesJson(
            output, expectedOutputJsonFile, EncodePasswords.ALL, MultiAuxEncoder(
                aux2 to StringAuxEncoder(),
                aux3 to StringAuxEncoder()
            )
        )
    }
}
