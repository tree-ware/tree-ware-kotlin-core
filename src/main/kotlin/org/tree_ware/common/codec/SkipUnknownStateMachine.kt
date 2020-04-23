package org.tree_ware.common.codec

import org.tree_ware.model.codec.decoding_state_machine.DecodingStack

class SkipUnknownStateMachine(private val stack: DecodingStack) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        stack.addFirst(SkipUnknownStateMachine(stack))
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
        // Remove self from stack
        stack.pollFirst()
        return true
    }
}
