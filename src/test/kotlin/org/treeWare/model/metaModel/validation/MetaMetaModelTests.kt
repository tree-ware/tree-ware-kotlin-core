package org.treeWare.model.metaModel.validation

import org.treeWare.metaModel.newMainMetaMetaModel
import org.treeWare.metaModel.validation.validate
import kotlin.test.Test
import kotlin.test.assertTrue

class MetaMetaModelTests {
    @Test
    fun `Meta-meta-model must be valid`() {
        val errors = validate(newMainMetaMetaModel())
        assertTrue(errors.isEmpty())
    }
}
