package org.treeWare.metaModel

import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.Resolved
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.getFileReader
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val metaMetaModel = newMetaMetaModel()

fun assertJsonStringValidationErrors(metaModelJsonString: String, expectedValidationErrors: List<String>) {
    val stringReader = StringReader(metaModelJsonString)
    assertJsonValidationErrors(stringReader, expectedValidationErrors)
}

fun assertJsonFileValidationErrors(metaModelJsonFile: String, expectedValidationErrors: List<String>) {
    val fileReader = getFileReader(metaModelJsonFile)
    assertNotNull(fileReader)
    assertJsonValidationErrors(fileReader, expectedValidationErrors)
}

private fun assertJsonValidationErrors(jsonReader: Reader, expectedValidationErrors: List<String>) {
    val metaModel = decodeJson<Resolved>(jsonReader, metaMetaModel, "data") { null }
    val errors = if (metaModel != null) validate(metaModel)
    else listOf("Meta-model decoding failed")
    assertEquals(expectedValidationErrors.joinToString("\n"), errors.joinToString("\n"))
}
