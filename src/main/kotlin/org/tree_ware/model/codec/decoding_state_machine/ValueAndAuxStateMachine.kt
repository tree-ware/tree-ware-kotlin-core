package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.DecodingStateMachine

const val VALUE_KEY = "value"

class ValueAndAuxStateMachine(
    private val isListElement: Boolean,
    private val valueStateMachine: DecodingStateMachine,
    private val auxStateMachine: DecodingStateMachine,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    private val logger = LogManager.getLogger()

    override fun decodeKey(name: String): Boolean {
        setKeyState(name)
        if (keyName == VALUE_KEY) stack.addFirst(valueStateMachine)
        else stack.addFirst(auxStateMachine)
        return true
    }

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (!isListElement) {
            // Remove self from stack
            stack.pollFirst()
        }
        return true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        if (isListElement) {
            // End of the list needs to be handled by parent state machine.
            // So remove self from stack and call decodeListEnd() on previous
            // state machine.
            stack.pollFirst()
            val parentStateMachine = stack.peekFirst()
            if (parentStateMachine == null) {
                logger.error("No parent decoding state machine")
                return false
            }
            return parentStateMachine.decodeListEnd()
        } else {
            // This method should never get called
            assert(false)
            return false
        }
    }
}
