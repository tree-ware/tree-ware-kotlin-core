package org.tree_ware.model.codec

import org.tree_ware.model.core.ModelType
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.newAddressBookSchema
import org.tree_ware.schema.core.validate
import java.io.File
import java.io.FileReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonCodecTests {
    @Test
    fun `JSON codec data round trip must be lossless`() {
        testRoundTrip<Unit>("src/test/resources/model/address_book_1.json")
    }

    @Test
    fun `JSON decoder can decode values alone in error-all model`() {
        testRoundTrip<String>(
            "src/test/resources/model/address_book_error_all_model.json",
            true,
            "src/test/resources/model/address_book_1.json",
            ModelType.data
        )
    }

    @Test
    fun `JSON codec person filter-branch round trip must be lossless`() {
        testRoundTrip<Unit>("src/test/resources/model/address_book_filter_person_model.json")
    }

    @Test
    fun `JSON codec settings filter-branch round trip must be lossless`() {
        testRoundTrip<Unit>("src/test/resources/model/address_book_filter_settings_model.json")
    }

    @Test
    fun `JSON codec filter-all round trip must be lossless`() {
        testRoundTrip<Unit>("src/test/resources/model/address_book_filter_all_model.json")
    }

    private fun <Aux> testRoundTrip(
        inputFilePath: String,
        decodeAux: Boolean = false,
        expectedOutputFilePath: String? = null,
        forceDecodedModelType: ModelType? = null
    ) {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val inputFile = File(inputFilePath)
        assertTrue(inputFile.exists())

        val jsonReader = FileReader(inputFile)
        val model = MutableModel<Aux>(schema)
        val isDecoded = decodeJson(jsonReader, model, decodeAux)
        jsonReader.close()
        assertTrue(isDecoded)

        forceDecodedModelType?.also { model.type = it }

        val jsonWriter = StringWriter()
        val isEncoded = encodeJson(model, jsonWriter, true)
        assertTrue(isEncoded)

        val expected = if (expectedOutputFilePath == null) {
            // Use input file
            inputFile.readText()
        } else {
            val expectedFile = File(expectedOutputFilePath)
            assertTrue(expectedFile.exists())
            expectedFile.readText()
        }
        val actual = jsonWriter.toString()
        assertEquals(expected, actual)
    }
}
