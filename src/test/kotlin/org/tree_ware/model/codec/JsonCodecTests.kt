package org.tree_ware.model.codec

import org.tree_ware.model.codec.aux_encoder.AuxEncoder
import org.tree_ware.model.codec.aux_encoder.ErrorAuxEncoder
import org.tree_ware.model.codec.decoding_state_machine.AuxDecodingStateMachine
import org.tree_ware.model.codec.decoding_state_machine.DecodingStack
import org.tree_ware.model.codec.decoding_state_machine.ErrorAuxStateMachine
import org.tree_ware.model.getModel
import org.tree_ware.schema.core.newAddressBookSchema
import org.tree_ware.schema.core.validate
import java.io.InputStreamReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
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
            ErrorAuxEncoder(),
            "error"
        ) { ErrorAuxStateMachine(it) }
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
        auxEncoder: AuxEncoder? = null,
        expectedModelType: String = "data",
        auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>? = { null }
    ) {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val model =
            getModel(schema, inputFilePath, expectedModelType, auxStateMachineFactory)

        val jsonWriter = StringWriter()
        val isEncoded = try {
            encodeJson(model, auxEncoder, jsonWriter, true)
        } catch (e: Throwable) {
            e.printStackTrace()
            println("Encoded so far:")
            println(jsonWriter.toString())
            println("End of encoded")
            false
        }
        assertTrue(isEncoded)

        val fileReader = InputStreamReader(ClassLoader.getSystemResourceAsStream(inputFilePath))
        val expected = fileReader.readText()
        fileReader.close()
        val actual = jsonWriter.toString()
        assertEquals(expected, actual)
    }
}
