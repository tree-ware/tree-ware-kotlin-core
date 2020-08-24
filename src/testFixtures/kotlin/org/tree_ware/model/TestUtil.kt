package org.tree_ware.model

import org.tree_ware.model.codec.aux_encoder.AuxEncoder
import org.tree_ware.model.codec.decodeJson
import org.tree_ware.model.codec.decoding_state_machine.AuxDecodingStateMachine
import org.tree_ware.model.codec.decoding_state_machine.DecodingStack
import org.tree_ware.model.codec.encodeJson
import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.Schema
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun getFileReader(filePath: String): Reader? =
    ClassLoader.getSystemResourceAsStream(filePath)?.let { InputStreamReader(it) }

fun <Aux> getModel(
    schema: Schema,
    inputFilePath: String,
    expectedModelType: String = "data",
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>? = { null }
): MutableModel<Aux> {
    val fileReader = getFileReader(inputFilePath)
    assertNotNull(fileReader)
    val model = decodeJson(fileReader, schema, expectedModelType, auxStateMachineFactory)
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

    val expectedFileReader = getFileReader(jsonFilePath)
    assertNotNull(expectedFileReader)
    val expected = expectedFileReader.readText()
    expectedFileReader.close()

    val actual = jsonWriter.toString()
    assertEquals(expected, actual)
}
