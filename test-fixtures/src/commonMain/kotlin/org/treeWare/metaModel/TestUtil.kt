package org.treeWare.metaModel

import okio.Buffer
import okio.BufferedSource
import okio.buffer
import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.decodeJsonEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.util.getFileSource
import kotlin.test.assertEquals

fun assertJsonStringValidationErrors(
    metaModelJsonString: String,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String> = emptyList(),
    options: ModelDecoderOptions = ModelDecoderOptions(),
    vararg auxPlugins: MetaModelAuxPlugin
) {
    val bufferedSource = Buffer().writeUtf8(metaModelJsonString)
    return assertJsonValidationErrors(
        bufferedSource,
        expectedValidationErrors,
        expectedDecodeErrors,
        options,
        *auxPlugins
    )
}

fun assertJsonFileValidationErrors(
    metaModelJsonFile: String,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String> = emptyList(),
    options: ModelDecoderOptions = ModelDecoderOptions(),
    vararg auxPlugins: MetaModelAuxPlugin
) = getFileSource(metaModelJsonFile).use {
    assertJsonValidationErrors(
        it.buffer(),
        expectedValidationErrors,
        expectedDecodeErrors,
        options,
        *auxPlugins
    )
}

private fun assertJsonValidationErrors(
    bufferedSource: BufferedSource,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String>,
    options: ModelDecoderOptions,
    vararg auxPlugins: MetaModelAuxPlugin
) {
    val multiAuxDecodingStateMachineFactory =
        MultiAuxDecodingStateMachineFactory(*auxPlugins.map { it.auxName to it.auxDecodingStateMachineFactory }
            .toTypedArray())
    val metaModel = metaModelRootEntityFactory(null)
    val decodeErrors = decodeJsonEntity(
        bufferedSource,
        metaModel,
        options,
        multiAuxDecodingStateMachineFactory
    )
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    if (decodeErrors.isEmpty()) {
        val errors = validate(metaModel, auxPlugins)
        assertEquals(expectedValidationErrors.joinToString("\n"), errors.joinToString("\n"))
    }
}

private fun validate(metaModel: MutableEntityModel, auxPlugins: Array<out MetaModelAuxPlugin>): List<String> {
    val baseErrors = validate(metaModel, null, null, ::metaModelRootEntityFactory)
    if (baseErrors.isNotEmpty()) return baseErrors
    return auxPlugins.flatMap { it.validate(metaModel) }
}