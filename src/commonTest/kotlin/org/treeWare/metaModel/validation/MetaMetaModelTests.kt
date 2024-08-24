package org.treeWare.metaModel.validation

import org.treeWare.metaModel.newMetaMetaModel
import kotlin.test.Test

class MetaMetaModelTests {
    @Test
    fun `Meta-meta-model must be valid`() {
        newMetaMetaModel()
    }
}
