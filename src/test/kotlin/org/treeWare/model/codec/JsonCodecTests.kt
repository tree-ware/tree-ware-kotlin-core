package org.treeWare.model.codec

import org.treeWare.model.assertMatchesJson
import org.treeWare.model.codec.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.codec.decoder.stateMachine.DecodingStack
import org.treeWare.model.codec.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.codec.encoder.AuxEncoder
import org.treeWare.model.codec.encoder.ErrorAuxEncoder
import org.treeWare.model.getModel
import org.treeWare.schema.core.newAddressBookSchema
import org.treeWare.schema.core.validate
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonCodecTests {
    @Test
    fun `JSON codec data round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_1.json")
    }

    @Test
    fun `JSON codec error-model round trip must be lossless`() {
        testRoundTrip(
            "model/address_book_error_all_model.json",
            null,
            ErrorAuxEncoder(),
            "error"
        ) { StringAuxStateMachine(it) }
    }

    @Test
    fun `JSON codec person filter-branch round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_filter_person_model.json")
    }

    @Test
    fun `JSON codec settings filter-branch round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_filter_settings_model.json")
    }

    @Test
    fun `JSON codec filter-all round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_filter_all_model.json")
    }

    private fun <Aux> testRoundTrip(
        inputFilePath: String,
        outputFilePath: String? = null,
        auxEncoder: AuxEncoder? = null,
        expectedModelType: String = "data",
        auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>? = { null }
    ) {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val model = getModel(schema, inputFilePath, expectedModelType, auxStateMachineFactory)
        assertMatchesJson(model, auxEncoder, outputFilePath ?: inputFilePath)
    }
}
