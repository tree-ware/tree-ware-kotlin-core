package org.tree_ware.model.codec

import org.tree_ware.common.codec.JsonWireFormatDecoder
import org.tree_ware.model.codec.decoding_state_machine.ModelDecodingStateMachine
import org.tree_ware.model.core.MutableModel
import java.io.Reader

fun decode(reader: Reader, model: MutableModel): Boolean {
    val decodingStateMachine = ModelDecodingStateMachine(model)
    val wireFormatDecoder = JsonWireFormatDecoder()
    return wireFormatDecoder.decode(reader, decodingStateMachine)
}
