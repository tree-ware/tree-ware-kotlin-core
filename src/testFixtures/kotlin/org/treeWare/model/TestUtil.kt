package org.treeWare.model

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.encodeJson
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun getFileReader(filePath: String): Reader? =
    ClassLoader.getSystemResourceAsStream(filePath)?.let { InputStreamReader(it) }

fun readFile(filePath: String): String? {
    val reader = getFileReader(filePath)
    val text = reader?.readText()
    reader?.close()
    return text
}

fun <Aux> getMainModel(
    meta: MainModel<Resolved>,
    inputFilePath: String,
    expectedModelType: String = "data",
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>? = { null }
): MutableMainModel<Aux> {
    val fileReader = getFileReader(inputFilePath)
    assertNotNull(fileReader)
    val model = decodeJson(fileReader, meta, expectedModelType, auxStateMachineFactory)
    fileReader.close()
    assertTrue(model != null)
    return model
}

/** Encodes the model element to JSON and asserts that it matches the JSON in the file.
 */
fun <Aux> assertMatchesJson(element: ElementModel<Aux>, auxEncoder: AuxEncoder?, jsonFilePath: String) {
    val jsonWriter = StringWriter()
    val isEncoded = try {
        encodeJson(element, auxEncoder, jsonWriter, true)
    } catch (e: Throwable) {
        e.printStackTrace()
        println("Encoded so far:")
        println(jsonWriter.toString())
        println("End of encoded")
        false
    }
    assertTrue(isEncoded)

    val expected = readFile(jsonFilePath)
    assertNotNull(expected)

    val actual = jsonWriter.toString()
    assertEquals(expected, actual)
}
