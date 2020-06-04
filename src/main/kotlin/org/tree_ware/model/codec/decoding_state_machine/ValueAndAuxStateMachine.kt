package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine

const val VALUE_KEY = "value"

class ValueAndAuxStateMachine<Aux>(
    private val isListElement: Boolean,
    private val valueStateMachine: ValueDecodingStateMachine<Aux>,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    private val auxStateMachine: AuxDecodingStateMachine<Aux>? = auxStateMachineFactory()
    private val logger = LogManager.getLogger()

    init {
        assert(auxStateMachine != null)
    }

    override fun decodeKey(name: String): Boolean {
        setKeyState(name)
        if (keyName == VALUE_KEY) stack.addFirst(valueStateMachine)
        else auxStateMachine?.also {
            stack.addFirst(it)
            it.decodeKey(name)
        }
        return true
    }

    override fun decodeObjectStart(): Boolean {
        auxStateMachine?.newAux()
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        auxStateMachine?.getAux()?.also { valueStateMachine.setAux(it) }
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
