package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.Schema

class ModelStateMachine<Aux>(
    private val schema: Schema,
    private val expectedModelType: String,
    private val auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    var model: MutableModel<Aux>? = null
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

        if (keyName != expectedModelType) return false
        if (model == null) model = MutableModel(schema)
        model?.also { decodeModel(expectedModelType, it) { auxStateMachineFactory(stack) } }
        return true
    }

    private fun decodeModel(
        modelType: String,
        newModel: MutableModel<Aux>,
        auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
    ) {
        newModel.type = modelType
        val root = newModel.getOrNewRoot()
        stack.addFirst(RootModelStateMachine(root, stack, auxStateMachineFactory))
    }
}
