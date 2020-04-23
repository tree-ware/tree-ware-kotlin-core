package org.tree_ware.schema.codec

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.tree_ware.schema.core.SchemaManager
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
