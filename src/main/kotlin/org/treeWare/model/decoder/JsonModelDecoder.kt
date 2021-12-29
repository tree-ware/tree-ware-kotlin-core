package org.treeWare.model.decoder

import org.treeWare.model.core.MainModel
import org.treeWare.model.decoder.stateMachine.ModelDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import java.io.Reader

fun decodeJson(
    reader: Reader,
    meta: MainModel,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory()
): ModelDecoderResult {
    val decodingStateMachine = ModelDecodingStateMachine(meta, options, multiAuxDecodingStateMachineFactory)
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decoded = wireFormatDecoder.decode(reader, decodingStateMachine)
    val mainModel = if (decoded) decodingStateMachine.mainModel else null
    return ModelDecoderResult(mainModel, decodingStateMachine.errors)
}
