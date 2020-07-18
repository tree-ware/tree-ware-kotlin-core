package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.Schema

class ModelStateMachine(
    private val schema: Schema, private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    var model: MutableModel<out Any>? = null
        private set

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        // TODO(deepak-nulu): expected-model-type & aux-state-machine as constructor parameters for custom aux types
        when (val modelType = keyName) {
            "data" -> decodeModel(modelType, MutableModel<Unit>(schema)) { null }
            "error" -> decodeModel(modelType, MutableModel<String>(schema)) { ErrorAuxStateMachine(stack) }
            null -> decodeModel("data", MutableModel<Unit>(schema)) { SkipUnknownStateMachine<Unit>(stack) }
        }
        return true
    }

    private fun <Aux> decodeModel(
        modelType: String,
        newModel: MutableModel<Aux>,
        auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
    ) {
        newModel.type = modelType
        val root = newModel.getOrNewRoot()
        stack.addFirst(RootModelStateMachine(root, stack, auxStateMachineFactory))
        model = newModel as MutableModel<out Any>
    }
}
