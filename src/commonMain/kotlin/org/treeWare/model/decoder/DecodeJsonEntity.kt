package org.treeWare.model.decoder

import okio.BufferedSource
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decoder.stateMachine.BaseEntityStateMachine
import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory


/**
 * Decode the JSON in `bufferedSource` into `entity`.
 *
 * @return list of errors.
 */
fun decodeJsonEntity(
    bufferedSource: BufferedSource,
    entity: MutableEntityModel,
    options: ModelDecoderOptions = ModelDecoderOptions(),
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(),
    debug: Boolean = false
): List<String> {
    val delegatingStateMachine = DelegatingStateMachine(debug) { errors, stack ->
        BaseEntityStateMachine(null, { entity }, stack, options, errors, multiAuxDecodingStateMachineFactory, null)
    }
    val wireFormatDecoder = JsonWireFormatDecoder()
    val decodeError = try {
        wireFormatDecoder.decode(bufferedSource, delegatingStateMachine)
    } catch (exception: Exception) {
        exception.message ?: "Exception while decoding JSON entity"
    }
    return if (delegatingStateMachine.errors.isNotEmpty()) delegatingStateMachine.errors
    else if (decodeError != null) listOf(decodeError)
    else emptyList()
}