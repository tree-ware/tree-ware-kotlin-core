package org.tree_ware.model.codec

import org.tree_ware.common.codec.JsonWireFormatDecoder
import org.tree_ware.model.codec.decoding_state_machine.AuxDecodingStateMachine
import org.tree_ware.model.codec.decoding_state_machine.DecodingStack
import org.tree_ware.model.codec.decoding_state_machine.ModelDecodingStateMachine
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.Schema
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
