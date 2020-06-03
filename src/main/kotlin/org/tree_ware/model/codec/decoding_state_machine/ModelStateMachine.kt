package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.ModelType
import org.tree_ware.model.core.MutableModel

class ModelStateMachine<Aux>(
    private val model: MutableModel<Aux>, private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
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

        val modelType = try {
            enumValueOf<ModelType>(keyName ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
        if (modelType != null) {
            model.type = modelType
            val root = model.getOrNewRoot()
            stack.addFirst(RootModelStateMachine(root, stack, getAuxStateMachine(modelType)))
        } else {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
        }
        return true
    }

    private fun getAuxStateMachine(modelType: ModelType): AuxDecodingStateMachine<Aux>? = when (modelType) {
        ModelType.data -> null
        else -> SkipUnknownStateMachine(stack)
    }
}
