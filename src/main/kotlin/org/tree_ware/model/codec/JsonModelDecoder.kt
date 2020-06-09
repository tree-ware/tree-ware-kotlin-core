package org.tree_ware.model.codec

import org.tree_ware.common.codec.JsonWireFormatDecoder
import org.tree_ware.model.codec.decoding_state_machine.ModelDecodingStateMachine
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.Schema
import java.io.Reader

fun decodeJson(reader: Reader, schema: Schema): MutableModel<out Any>? {
    val decodingStateMachine = ModelDecodingStateMachine(schema)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    return if (decoded) decodingStateMachine.model else null
}
