package org.treeWare.model

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.encodeJson
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun testRoundTrip(
    inputFilePath: String,
    outputFilePath: String? = null,
    auxEncoder: AuxEncoder? = null,
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedModelType: String = "data",
    expectedDecodeErrors: List<String> = listOf(),
    hasher: Hasher? = null,
    cipher: Cipher? = null,
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine? = { null }
) {
    val metaModel = newAddressBookMetaModel(hasher, cipher)

    val model =
        getMainModelFromJsonFile(
            metaModel,
            inputFilePath,
            options,
            expectedModelType,
            expectedDecodeErrors,
            auxStateMachineFactory
        )
    assertMatchesJson(model, auxEncoder, outputFilePath ?: inputFilePath, encodePasswords)
}

fun getFileReader(filePath: String): Reader =
    ClassLoader.getSystemResourceAsStream(filePath)?.let { InputStreamReader(it) }
        ?: throw IllegalArgumentException("File $filePath not found")

fun readFile(filePath: String): String {
    val reader = getFileReader(filePath)
    val text = reader.readText()
    reader.close()
    return text
}

fun getMainModelFromJsonString(
    meta: MainModel,
    jsonString: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedModelType: String = "data",
    expectedDecodeErrors: List<String> = listOf(),
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine? = { null }
): MutableMainModel = getMainModelFromJson(
    meta,
    StringReader(jsonString),
    options,
    expectedModelType,
    expectedDecodeErrors,
    auxStateMachineFactory
)

fun getMainModelFromJsonFile(
    meta: MainModel,
    jsonFilePath: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedModelType: String = "data",
    expectedDecodeErrors: List<String> = listOf(),
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine? = { null }
): MutableMainModel = getMainModelFromJson(
    meta,
    getFileReader(jsonFilePath),
    options,
    expectedModelType,
    expectedDecodeErrors,
    auxStateMachineFactory
)

fun getMainModelFromJson(
    meta: MainModel,
    jsonReader: Reader,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedModelType: String = "data",
    expectedDecodeErrors: List<String> = listOf(),
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine? = { null }
): MutableMainModel {
    val (mainModel, decodeErrors) = decodeJson(
        jsonReader,
        meta,
        expectedModelType,
        options,
        auxStateMachineFactory
    )
    jsonReader.close()
    assertTrue(mainModel != null)
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    return mainModel
}

/** Encodes the model element to JSON and asserts that it matches the JSON in the file.
 */
fun assertMatchesJson(
    element: ElementModel,
    auxEncoder: AuxEncoder?,
    jsonFilePath: String,
    encodePasswords: EncodePasswords
) {
    val jsonWriter = StringWriter()
    val isEncoded = try {
        encodeJson(element, auxEncoder, jsonWriter, encodePasswords, true)
    } catch (e: Throwable) {
        e.printStackTrace()
        println("Encoded so far:")
        println(jsonWriter.toString())
        println("End of encoded")
        false
    }
    assertTrue(isEncoded)

    val expected = readFile(jsonFilePath)
    val actual = jsonWriter.toString()
    assertEquals(expected, actual)
}
