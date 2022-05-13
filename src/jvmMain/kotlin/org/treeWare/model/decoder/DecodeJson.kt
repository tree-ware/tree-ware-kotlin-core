package org.treeWare.model.decoder

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import org.treeWare.model.decoder.stateMachine.MainModelStateMachine
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import java.io.Reader

fun decodeJson(
    reader: Reader,
    mainMeta: MainModel,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    debug: Boolean = false
): ModelDecoderResult {
    val mainModel = MutableMainModel(mainMeta)
    val delegatingStateMachine = DelegatingStateMachine(debug) { errors, stack ->
        MainModelStateMachine(mainModel, stack, options, errors, multiAuxDecodingStateMachineFactory)
    }
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decodeError = try {
        wireFormatDecoder.decode(reader, delegatingStateMachine)
    } catch (exception: Exception) {
        exception.message ?: "Exception while decoding JSON"
    }
    return if (delegatingStateMachine.errors.isNotEmpty()) ModelDecoderResult(mainModel, delegatingStateMachine.errors)
    else if (decodeError != null) return ModelDecoderResult(null, listOf(decodeError))
    else ModelDecoderResult(mainModel, delegatingStateMachine.errors)
}