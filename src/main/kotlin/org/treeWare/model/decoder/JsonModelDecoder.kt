package org.treeWare.model.decoder

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.model.decoder.stateMachine.ModelDecodingStateMachine
import java.io.Reader

fun <Aux> decodeJson(
    reader: Reader,
    meta: MainModel<Resolved>,
    expectedModelType: String,
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?,
): MutableMainModel<Aux>? {
    val decodingStateMachine = ModelDecodingStateMachine(meta, expectedModelType, auxStateMachineFactory)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    return if (decoded) decodingStateMachine.mainModel else null
}
