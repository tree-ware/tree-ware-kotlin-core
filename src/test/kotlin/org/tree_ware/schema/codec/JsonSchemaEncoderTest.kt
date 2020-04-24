package org.tree_ware.schema.codec

import org.tree_ware.schema.core.validate
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonSchemaEncoderTest {
    @Test
    fun `JsonSchemaEncoder pretty-prints the schema`() {
        val schema = getGoldenKotlinSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val stringWriter = StringWriter()
        val isEncoded = encodeJson(schema, stringWriter, true)
        val encodedSchema = stringWriter.toString()

        assertTrue(isEncoded)
        assertEquals(goldenJsonPrettyPrintedSchema, encodedSchema)
    }
}
