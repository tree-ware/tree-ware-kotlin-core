package org.tree_ware.model.codec

import org.tree_ware.model.core.ModelType
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
        testRoundTrip("src/test/resources/model/address_book_1.json")
    }

    @Test
    fun `JSON codec error-model round trip must be lossless`() {
        testRoundTrip("src/test/resources/model/address_book_error_all_model.json")
    }

    @Test
    fun `JSON codec person filter-branch round trip must be lossless`() {
        testRoundTrip("src/test/resources/model/address_book_filter_person_model.json")
    }

    @Test
    fun `JSON codec settings filter-branch round trip must be lossless`() {
        testRoundTrip("src/test/resources/model/address_book_filter_settings_model.json")
    }

    @Test
    fun `JSON codec filter-all round trip must be lossless`() {
        testRoundTrip("src/test/resources/model/address_book_filter_all_model.json")
    }

    private fun testRoundTrip(
        inputFilePath: String,
        expectedOutputFilePath: String? = null,
        forceDecodedModelType: ModelType? = null
    ) {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val inputFile = File(inputFilePath)
        assertTrue(inputFile.exists())

        val jsonReader = FileReader(inputFile)
        val model = decodeJson(jsonReader, schema)
        jsonReader.close()
        assertTrue(model != null)

        forceDecodedModelType?.also { model.type = it }

        val jsonWriter = StringWriter()
        val isEncoded = try {
            encodeJson(model, jsonWriter, true)
        } catch (e: Throwable) {
            e.printStackTrace()
            println("Encoded so far:")
            println(jsonWriter.toString())
            println("End of encoded")
            false
        }
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
