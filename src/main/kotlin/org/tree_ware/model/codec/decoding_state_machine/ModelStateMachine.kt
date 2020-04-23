package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.ModelType
import org.tree_ware.model.core.MutableModel

class ModelStateMachine(
    private val model: MutableModel, private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        val modelType = try {
            enumValueOf<ModelType>(keyName ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
        if (modelType != null) {
            model.type = modelType
            val root = model.getOrNewRoot()
            stack.addFirst(RootModelStateMachine(root, stack))
        } else {
            stack.addFirst(SkipUnknownStateMachine(stack))
        }
        resetKeyState()
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        stack.addFirst(SkipUnknownStateMachine(stack))
        resetKeyState()
        return true
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }
}
