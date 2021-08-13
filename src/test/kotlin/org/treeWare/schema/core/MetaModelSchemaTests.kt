package org.treeWare.schema.core

import kotlin.test.Test
import kotlin.test.assertTrue

class MetaModelSchemaTests {
    @Test
    fun `Meta-model schema is valid`() {
        val errors = validate(metaModelSchema)
        assertTrue(errors.isEmpty())
    }
}
