package org.treeWare.model.codec

import okio.Buffer
import okio.buffer
import org.treeWare.metaModel.getResolvedRootMeta
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.decodeJsonEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.encodePaths
import org.treeWare.util.getFileSource
import kotlin.test.Test
import kotlin.test.assertEquals

class PathCodecTests {

    @Test
    fun path_codec_data_round_trip_must_be_lossless() {
        val entityMeta = newAddressBookMetaModel().metaModel
            ?.let { getResolvedRootMeta(it) }
            ?: error("Meta-model has validation errors")

        val entity = MutableEntityModel(entityMeta, null)

        getFileSource("model/address_book_1.json").use { source ->
            decodeJsonEntity(
                source.buffer(),
                entity,
                ModelDecoderOptions(),
                MultiAuxDecodingStateMachineFactory()
            )
        }

        val model: ElementModel = entity

        val sink = Buffer()
        encodePaths(model, sink)

        val actualPaths = sink.readUtf8()

        val expectedPaths = getFileSource("model/address_book_1.txt").use {
            it.buffer().readUtf8()
        }

        assertEquals(expectedPaths, actualPaths)
    }
}
