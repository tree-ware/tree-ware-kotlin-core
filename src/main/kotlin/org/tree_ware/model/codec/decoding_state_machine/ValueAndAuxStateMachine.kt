package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.DecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine

const val VALUE_KEY = "value"

class ValueAndAuxStateMachine(
    private val valueStateMachine: DecodingStateMachine, private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    override fun decodeKey(name: String): Boolean {
        setKeyState(name)
        if (keyName == VALUE_KEY) stack.addFirst(valueStateMachine)
        else stack.addFirst(SkipUnknownStateMachine(stack))
        return true
    }

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
}
