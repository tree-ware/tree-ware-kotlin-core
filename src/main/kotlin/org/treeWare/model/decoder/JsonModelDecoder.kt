package org.treeWare.model.decoder

import org.treeWare.model.core.Model
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.model.decoder.stateMachine.ModelDecodingStateMachine
import java.io.Reader

fun <Aux> decodeJson(
    reader: Reader,
    meta: Model<Resolved>,
    expectedModelType: String,
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?
): MutableModel<Aux>? {
    val decodingStateMachine = ModelDecodingStateMachine(meta, expectedModelType, auxStateMachineFactory)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    return if (decoded) decodingStateMachine.model else null
}
