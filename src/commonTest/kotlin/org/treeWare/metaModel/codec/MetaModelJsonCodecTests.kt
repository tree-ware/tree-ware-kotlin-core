package org.treeWare.metaModel.codec

import org.treeWare.metaModel.ADDRESS_BOOK_META_MODEL_FILES
import org.treeWare.metaModel.metaModelRootEntityFactory
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class MetaModelJsonCodecTests {
    @Test
    fun `Meta-model JSON codec round trip must be lossless`() {
        ADDRESS_BOOK_META_MODEL_FILES.forEach {
            val metaModel = metaModelRootEntityFactory(null)
            testRoundTrip(it, entity = metaModel)
        }
    }
}