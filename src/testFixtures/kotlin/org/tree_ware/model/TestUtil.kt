package org.tree_ware.model

import org.tree_ware.model.codec.decodeJson
import org.tree_ware.model.codec.decoding_state_machine.AuxDecodingStateMachine
import org.tree_ware.model.codec.decoding_state_machine.DecodingStack
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.Schema
import java.io.InputStreamReader
import kotlin.test.assertTrue

fun <Aux> getModel(
    schema: Schema,
    inputFilePath: String,
    expectedModelType: String = "data",
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>? = { null }
): MutableModel<Aux> {
    val fileReader = InputStreamReader(ClassLoader.getSystemResourceAsStream(inputFilePath))
    val model = decodeJson(fileReader, schema, expectedModelType, auxStateMachineFactory)
    fileReader.close()
    assertTrue(model != null)
    return model
}
