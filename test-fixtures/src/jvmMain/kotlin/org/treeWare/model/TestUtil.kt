package org.treeWare.model

import okio.Buffer
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodeJson
import org.treeWare.util.getFileReader
import org.treeWare.util.readFile
import java.io.Reader
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun testRoundTrip(
    inputFilePath: String,
    outputFilePath: String? = null,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    hasher: Hasher? = null,
    cipher: Cipher? = null,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    metaModel: MainModel = newAddressBookMetaModel(hasher, cipher).metaModel
        ?: throw IllegalStateException("Meta-model has validation errors")
) {
    val model =
        getMainModelFromJsonFile(
            metaModel,
            inputFilePath,
            options,
            expectedDecodeErrors,
            multiAuxDecodingStateMachineFactory
        )
    assertMatchesJson(model, outputFilePath ?: inputFilePath, encodePasswords, multiAuxEncoder)
}

fun getMainModelFromJsonString(
    meta: MainModel,
    jsonString: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory()
): MutableMainModel = getMainModelFromJson(
    meta,
    StringReader(jsonString),
    options,
    expectedDecodeErrors,
    multiAuxDecodingStateMachineFactory
)

fun getMainModelFromJsonFile(
    meta: MainModel,
    jsonFilePath: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory()
): MutableMainModel = getMainModelFromJson(
    meta,
    getFileReader(jsonFilePath),
    options,
    expectedDecodeErrors,
    multiAuxDecodingStateMachineFactory
)

fun getMainModelFromJson(
    meta: MainModel,
    jsonReader: Reader,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory()
): MutableMainModel {
    val (mainModel, decodeErrors) = decodeJson(
        jsonReader,
        meta,
        options,
        multiAuxDecodingStateMachineFactory
    )
    jsonReader.close()
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    assertTrue(mainModel != null)
    return mainModel
}

/** Encode the model element to JSON and assert that it matches the specified JSON file. */
fun assertMatchesJson(
    element: ElementModel,
    expectedJsonFilePath: String,
    encodePasswords: EncodePasswords,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder()
) {
    val expectedJsonString = readFile(expectedJsonFilePath)
    assertMatchesJsonString(element, expectedJsonString, encodePasswords, multiAuxEncoder)
}

/** Encode the model element to JSON and assert that it matches the specified JSON string. */
fun assertMatchesJsonString(
    element: ElementModel,
    expectedJsonString: String,
    encodePasswords: EncodePasswords,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder()
) {
    val actualJsonString = getEncodedJsonString(element, encodePasswords, multiAuxEncoder)
    assertEquals(expectedJsonString, actualJsonString)
}

/** Encode the model element to JSON and assert that it contains the specified JSON string. */
fun assertContainsJsonString(
    element: ElementModel,
    expectedJsonStringSnippet: String,
    encodePasswords: EncodePasswords,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder()
) {
    val actualJsonString = getEncodedJsonString(element, encodePasswords, multiAuxEncoder)
    assertTrue(actualJsonString.contains(expectedJsonStringSnippet))
}

fun getEncodedJsonString(
    element: ElementModel,
    encodePasswords: EncodePasswords,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder()
): String {
    val buffer = Buffer()
    val isEncoded = try {
        encodeJson(element, buffer, multiAuxEncoder, encodePasswords, true)
    } catch (e: Throwable) {
        e.printStackTrace()
        println("Encoded so far:")
        println(buffer.readUtf8())
        println("End of encoded")
        false
    }
    assertTrue(isEncoded)
    return buffer.readUtf8()
}