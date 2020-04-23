package org.tree_ware.schema.codec

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.tree_ware.schema.core.validate
import java.io.StringWriter

class JsonSchemaEncoderTest {
    @Test
    fun `JsonSchemaEncoder pretty-prints the schema`() {
        val schema = getGoldenKotlinSchema()
        val errors = validate(schema)
        assertThat(errors.isEmpty()).isTrue()

        val stringWriter = StringWriter()
        val isEncoded = encodeJson(schema, stringWriter, true)
        val encodedSchema = stringWriter.toString()

        assertThat(isEncoded).isTrue()
        assertThat(encodedSchema).isEqualTo(goldenJsonPrettyPrintedSchema)
    }
}
