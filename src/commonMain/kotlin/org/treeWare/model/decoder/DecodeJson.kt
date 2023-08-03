package org.treeWare.model.decoder

import okio.BufferedSource
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import org.treeWare.model.decoder.stateMachine.MainModelStateMachine
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory


/**
 * Decode the JSON in `bufferedSource` into `mainModel`.
 *
 * @return list of errors.
 */
fun decodeJson(
    bufferedSource: BufferedSource,
    mainModel: MutableMainModel,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    debug: Boolean = false
): List<String> {
    val delegatingStateMachine = DelegatingStateMachine(debug) { errors, stack ->
        MainModelStateMachine(mainModel, stack, options, errors, multiAuxDecodingStateMachineFactory)
    }
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decodeError = try {
        wireFormatDecoder.decode(bufferedSource, delegatingStateMachine)
    } catch (exception: Exception) {
        exception.message ?: "Exception while decoding JSON"
    }
    return if (delegatingStateMachine.errors.isNotEmpty()) delegatingStateMachine.errors
    else if (decodeError != null) return listOf(decodeError)
    else delegatingStateMachine.errors
}