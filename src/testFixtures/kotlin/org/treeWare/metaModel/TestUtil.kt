package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Resolved
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.getFileReader
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertEquals

private val metaMetaModel = newMainMetaMetaModel()

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
    val (metaModel, decodeErrors) = decodeJson<Resolved>(jsonReader, metaMetaModel, "data") { null }
    val errors = if (metaModel != null) validate(metaModel, null, null)
    else listOf("Meta-model decoding failed")
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    assertEquals(expectedValidationErrors.joinToString("\n"), errors.joinToString("\n"))
}
