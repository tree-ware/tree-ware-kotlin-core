package org.treeWare.model.codec

import org.treeWare.common.codec.JsonWireFormatDecoder
import org.treeWare.model.codec.decoding_state_machine.AuxDecodingStateMachine
import org.treeWare.model.codec.decoding_state_machine.DecodingStack
import org.treeWare.model.codec.decoding_state_machine.ModelDecodingStateMachine
import org.treeWare.model.core.MutableModel
import org.treeWare.schema.core.Schema
import java.io.Reader

fun <Aux> decodeJson(
    reader: Reader,
    schema: Schema,
    expectedModelType: String,
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?
): MutableModel<Aux>? {
    val decodingStateMachine = ModelDecodingStateMachine(schema, expectedModelType, auxStateMachineFactory)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    return if (decoded) decodingStateMachine.model else null
}
