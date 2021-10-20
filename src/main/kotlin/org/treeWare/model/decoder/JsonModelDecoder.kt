package org.treeWare.model.decoder

import org.treeWare.model.core.MainModel
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.model.decoder.stateMachine.ModelDecodingStateMachine
import java.io.Reader

fun decodeJson(
    reader: Reader,
    meta: MainModel,
    expectedModelType: String,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine?
): ModelDecoderResult {
    val decodingStateMachine =
        ModelDecodingStateMachine(meta, expectedModelType, options, auxStateMachineFactory)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    val mainModel = if (decoded) decodingStateMachine.mainModel else null
    return ModelDecoderResult(mainModel, decodingStateMachine.errors)
}
