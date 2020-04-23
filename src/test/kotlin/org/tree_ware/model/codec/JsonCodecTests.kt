package org.tree_ware.model.codec

import org.tree_ware.schema.core.RootSchema
import org.tree_ware.schema.core.Schema
import org.tree_ware.schema.core.SchemaManager
import org.tree_ware.schema.core.getAddressBookPackage
import org.tree_ware.model.core.MutableModel
import java.io.File
import java.io.FileReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonCodecTests {
    private val schema: Schema
    private val root: RootSchema

    init {
        val schemaManager = SchemaManager()
        schemaManager.addPackages(listOf(getAddressBookPackage()))
        schema = schemaManager.schema
        root = schemaManager.root
    }

    @Test
    fun `JSON codec round trip must be lossless`() {
        val jsonFile = File("src/test/resources/model/address_book_1.json")
        assertTrue(jsonFile.exists())

        val jsonReader = FileReader(jsonFile)
        val model = MutableModel(schema, root)
        val isDecoded = decode(jsonReader, model)
        jsonReader.close()
        assertTrue(isDecoded)

        val jsonWriter = StringWriter()
        val isEncoded = encode(model, jsonWriter, true)
        assertTrue(isEncoded)

        val expected = jsonFile.readText()
        val actual = jsonWriter.toString()
        assertEquals(expected, actual)
    }
}
