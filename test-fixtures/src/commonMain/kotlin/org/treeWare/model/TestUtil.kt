package org.treeWare.model

import okio.Buffer
import okio.BufferedSource
import okio.buffer
import org.treeWare.metaModel.getResolvedRootMeta
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.*
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.decodeJsonEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodeJson
import org.treeWare.util.getFileSource
import org.treeWare.util.readFile
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
    entityMeta: EntityModel = newAddressBookMetaModel(hasher, cipher).metaModel?.let { getResolvedRootMeta(it) }
        ?: throw IllegalStateException("Meta-model has validation errors"),
    entity: MutableEntityModel = MutableEntityModel(entityMeta, null)
) {
    decodeJsonFileIntoEntity(
        inputFilePath,
        options,
        expectedDecodeErrors,
        multiAuxDecodingStateMachineFactory,
        entity
    )
    assertMatchesJson(entity, outputFilePath ?: inputFilePath, encodePasswords, multiAuxEncoder)
}

fun decodeJsonStringIntoEntity(
    jsonString: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    entity: MutableEntityModel
) {
    val bufferedSource = Buffer().writeUtf8(jsonString)
    decodeJsonIntoEntity(
        bufferedSource,
        options,
        expectedDecodeErrors,
        multiAuxDecodingStateMachineFactory,
        entity
    )
}

fun decodeJsonFileIntoEntity(
    jsonFilePath: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    entity: MutableEntityModel
) = getFileSource(jsonFilePath).use {
    decodeJsonIntoEntity(
        it.buffer(),
        options,
        expectedDecodeErrors,
        multiAuxDecodingStateMachineFactory,
        entity
    )
}

fun decodeJsonIntoEntity(
    bufferedSource: BufferedSource,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    expectedDecodeErrors: List<String> = listOf(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    entity: MutableEntityModel
) {
    val decodeErrors = decodeJsonEntity(
        bufferedSource,
        entity,
        options,
        multiAuxDecodingStateMachineFactory
    )
    assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
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