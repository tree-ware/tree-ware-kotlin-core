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
    val decodeError = try {
        wireFormatDecoder.decode(reader, decodingStateMachine)
    } catch (exception: Exception) {
        exception.message ?: "Exception while decoding set-request"
    }
    return if (decodingStateMachine.errors.isNotEmpty()) ModelDecoderResult(
        decodingStateMachine.mainModel,
        decodingStateMachine.errors
    )
    else if (decodeError != null) return ModelDecoderResult(null, listOf(decodeError))
    else ModelDecoderResult(decodingStateMachine.mainModel, decodingStateMachine.errors)
}