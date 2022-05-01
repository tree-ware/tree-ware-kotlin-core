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
    val decoded = try {
        wireFormatDecoder.decode(reader, decodingStateMachine)
    } catch (exception: Exception) {
        return ModelDecoderResult(null, listOf(exception.message ?: "Exception while decoding set-request"))
    }
    val mainModel = if (decoded) decodingStateMachine.mainModel else null
    return ModelDecoderResult(mainModel, decodingStateMachine.errors)
}