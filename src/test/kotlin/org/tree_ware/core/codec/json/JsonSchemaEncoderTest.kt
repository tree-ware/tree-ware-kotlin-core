package org.tree_ware.core.codec.json

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import java.io.StringWriter

class JsonSchemaEncoderTest {
    @Test
    fun `JsonSchemaEncoder pretty-prints the schema`() {
        val stringWriter = StringWriter()

        val jsonSchemaEncoder = JsonSchemaEncoder(stringWriter, true)

        val schema = getGoldenKotlinSchema()
        val isEncoded = jsonSchemaEncoder.encode(schema)
        val encodedSchema = stringWriter.toString()

        assertThat(isEncoded).isTrue()
        assertThat(encodedSchema).isEqualTo(goldenJsonPrettyPrintedSchema)
    }
}
