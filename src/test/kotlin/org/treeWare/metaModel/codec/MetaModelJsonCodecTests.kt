package org.treeWare.metaModel.codec

import org.treeWare.metaModel.ADDRESS_BOOK_META_MODEL_FILES
import org.treeWare.metaModel.newMainMetaMetaModel
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class MetaModelJsonCodecTests {
    @Test
    fun `Meta-model JSON codec round trip must be lossless`() {
        val metaMetaModel = newMainMetaMetaModel()
        ADDRESS_BOOK_META_MODEL_FILES.forEach { testRoundTrip(it, metaModel = metaMetaModel) }
    }
}