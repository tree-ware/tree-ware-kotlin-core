package org.tree_ware.core.codec.json

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.tree_ware.core.schema.SchemaManager
import java.io.StringWriter

class JsonSchemaEncoderTest {
    @Test
    fun `JsonSchemaEncoder pretty-prints the schema`() {
        val schemaManager = SchemaManager()
        schemaManager.addPackages(getGoldenKotlinPackages())

        val stringWriter = StringWriter()
        val isEncoded = schemaManager.encodeJson(stringWriter, true)
        val encodedSchema = stringWriter.toString()

        assertThat(isEncoded).isTrue()
        assertThat(encodedSchema).isEqualTo(goldenJsonPrettyPrintedSchema)
    }
}
