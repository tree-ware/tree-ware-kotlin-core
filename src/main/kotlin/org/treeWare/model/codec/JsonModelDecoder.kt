package org.treeWare.model.codec

import org.treeWare.common.codec.JsonWireFormatDecoder
import org.treeWare.model.codec.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.codec.decoder.stateMachine.DecodingStack
import org.treeWare.model.codec.decoder.stateMachine.ModelDecodingStateMachine
import org.treeWare.model.core.Model
import org.treeWare.model.core.MutableModel
import org.treeWare.schema.core.Schema
import java.io.Reader

fun <Aux> decodeJson(
    reader: Reader,
    schema: Schema,
    meta: Model<Unit>?,
    expectedModelType: String,
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?
): MutableModel<Aux>? {
    val decodingStateMachine = ModelDecodingStateMachine(schema, meta, expectedModelType, auxStateMachineFactory)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    return if (decoded) decodingStateMachine.model else null
}
