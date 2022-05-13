package org.treeWare.model.decoder

import org.treeWare.model.core.MutableFieldModel
import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.decoder.stateMachine.getFieldStateMachine
import java.io.Reader

fun decodeJsonField(
    reader: Reader,
    field: MutableFieldModel,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    debug: Boolean = false
): List<String> {
    val delegatingStateMachine = DelegatingStateMachine(debug) { errors, stack ->
        getFieldStateMachine(field, errors, stack, options, multiAuxDecodingStateMachineFactory)
    }
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decodeError = try {
        wireFormatDecoder.decode(reader, delegatingStateMachine)
    } catch (exception: Exception) {
        exception.message ?: "Exception while decoding JSON field"
    }
    return if (delegatingStateMachine.errors.isNotEmpty()) delegatingStateMachine.errors
    else if (decodeError != null) listOf(decodeError)
    else emptyList()
}