package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableRootModel

class RootModelStateMachine<Aux>(
    private val root: MutableRootModel<Aux>, private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        if (keyName == root.schema.name) {
            stack.addFirst(BaseEntityStateMachine(root, root.schema.resolvedEntity, stack))
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
