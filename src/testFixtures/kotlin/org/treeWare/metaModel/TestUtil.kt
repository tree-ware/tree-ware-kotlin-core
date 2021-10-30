package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getFileReader
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.operator.union
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertEquals

private val metaMetaModel = newMainMetaMetaModel()

fun newMetaModelFromFiles(
    filePaths: List<String>,
    hasher: Hasher?,
    cipher: Cipher?,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory()
): MutableMainModel {
    val metaModelParts = filePaths.map {
        getMainModelFromJsonFile(
            metaMetaModel,
            it,
            multiAuxDecodingStateMachineFactory = multiAuxDecodingStateMachineFactory
        )
    }
    val metaModel = union(metaModelParts)
    val errors = validate(metaModel, hasher, cipher)
    if (errors.isNotEmpty()) throw IllegalStateException("Address-book meta-model is not valid")
    return metaModel
}

fun assertJsonStringValidationErrors(
    metaModelJsonString: String,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String> = listOf()
) = assertJsonValidationErrors(StringReader(metaModelJsonString), expectedValidationErrors, expectedDecodeErrors)


fun assertJsonFileValidationErrors(
    metaModelJsonFile: String,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String> = listOf()
) = assertJsonValidationErrors(getFileReader(metaModelJsonFile), expectedValidationErrors, expectedDecodeErrors)

private fun assertJsonValidationErrors(
    jsonReader: Reader,
    expectedValidationErrors: List<String>,
    expectedDecodeErrors: List<String>
) {
    val (metaModel, decodeErrors) = decodeJson(jsonReader, metaMetaModel, "data")
    val errors = if (metaModel != null) validate(metaModel, null, null)
    else listOf("Meta-model decoding failed")
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    assertEquals(expectedValidationErrors.joinToString("\n"), errors.joinToString("\n"))
}
