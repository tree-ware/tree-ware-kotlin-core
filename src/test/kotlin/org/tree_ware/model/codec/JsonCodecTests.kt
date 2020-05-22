package org.tree_ware.model.codec

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
        testRoundTrip("src/test/resources/model/address_book_1.json")
    }

    @Test
    fun `JSON codec filter-branch round trip must be lossless`() {
        testRoundTrip("src/test/resources/model/address_book_filter_person_model.json")
        testRoundTrip("src/test/resources/model/address_book_filter_settings_model.json")
    }

    @Test
    fun `JSON codec filter-all round trip must be lossless`() {
        testRoundTrip("src/test/resources/model/address_book_filter_all_model.json")
    }

    private fun testRoundTrip(filePath: String) {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val jsonFile = File(filePath)
        assertTrue(jsonFile.exists())

        val jsonReader = FileReader(jsonFile)
        val model = MutableModel(schema)
        val isDecoded = decodeJson(jsonReader, model)
        jsonReader.close()
        assertTrue(isDecoded)

        val jsonWriter = StringWriter()
        val isEncoded = encodeJson(model, jsonWriter, true)
        assertTrue(isEncoded)

        val expected = jsonFile.readText()
        val actual = jsonWriter.toString()
        assertEquals(expected, actual)
    }
}
