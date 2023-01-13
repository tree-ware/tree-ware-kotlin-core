package org.treeWare.metaModel.validation

import org.treeWare.metaModel.newMainMetaMetaModel
import kotlin.test.Test

class MetaMetaModelTests {
    @Test
    fun `Meta-meta-model must be valid`() {
        newMainMetaMetaModel()
    }
}
