package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.DecodingStateMachine

class ListValueStateMachine(
    private val listElementStateMachine: DecodingStateMachine,
    private val stack: DecodingStack,
    private val decodeAux: Boolean
) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        stack.addFirst(listElementStateMachine)
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }
}
