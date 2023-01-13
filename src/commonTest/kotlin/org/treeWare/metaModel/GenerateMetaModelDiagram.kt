package org.treeWare.metaModel

import org.treeWare.metaModel.encoder.encodeDot
import kotlin.test.Test

// TODO(deepak-nulu): make doc generation a gradle task

class GenerateMetaModelDiagram {
    @Test
    fun `Generate address-book meta-model diagram`() {
        encodeDot(addressBookMetaModel)
    }
}