package org.treeWare.metaModel

import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.util.getFileReader
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertEquals

private val metaMetaModel = newMainMetaMetaModel()

fun assertJsonStringValidationErrors(
    metaModelJsonString: String,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String> = listOf(),
    options: ModelDecoderOptions = ModelDecoderOptions(),
    vararg auxPlugins: MetaModelAuxPlugin
) = assertJsonValidationErrors(
    StringReader(metaModelJsonString),
    expectedValidationErrors,
    expectedDecodeErrors,
    options,
    *auxPlugins
)

fun assertJsonFileValidationErrors(
    metaModelJsonFile: String,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String> = listOf(),
    options: ModelDecoderOptions = ModelDecoderOptions(),
    vararg auxPlugins: MetaModelAuxPlugin
) = assertJsonValidationErrors(
    getFileReader(metaModelJsonFile),
    expectedValidationErrors,
    expectedDecodeErrors,
    options,
    *auxPlugins
)

private fun assertJsonValidationErrors(
    jsonReader: Reader,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String>,
    options: ModelDecoderOptions,
    vararg auxPlugins: MetaModelAuxPlugin
) {
    val multiAuxDecodingStateMachineFactory =
        MultiAuxDecodingStateMachineFactory(*auxPlugins.map { it.auxName to it.auxDecodingStateMachineFactory }
            .toTypedArray())
    val (metaModel, decodeErrors) = decodeJson(jsonReader, metaMetaModel, options, multiAuxDecodingStateMachineFactory)
    val errors = validate(metaModel, auxPlugins)
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    assertEquals(expectedValidationErrors.joinToString("\n"), errors.joinToString("\n"))
}

private fun validate(metaModel: MutableMainModel?, auxPlugins: Array<out MetaModelAuxPlugin>): List<String> {
    if (metaModel == null) return listOf("Meta-model decoding failed")
    val baseErrors = validate(metaModel, null, null)
    if (baseErrors.isNotEmpty()) return baseErrors
    return auxPlugins.flatMap { it.validate(metaModel) }
}