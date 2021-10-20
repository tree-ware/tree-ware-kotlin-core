package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager

const val VALUE_KEY = "value"

class ValueAndAuxStateMachine(
    private val isListElement: Boolean,
    private val valueStateMachine: ValueDecodingStateMachine,
    auxStateMachineFactory: () -> AuxDecodingStateMachine?,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    private val auxStateMachine: AuxDecodingStateMachine? = auxStateMachineFactory()
    private val logger = LogManager.getLogger()

    override fun decodeKey(name: String): Boolean {
        setKeyState(name)
        if (keyName == VALUE_KEY) stack.addFirst(valueStateMachine)
        else auxStateMachine?.also {
            stack.addFirst(it)
            it.decodeKey(name)
            auxStateMachine.newAux()
        }
        return true
    }

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        auxStateMachine?.also { valueStateMachine.setAux(it.auxType, it.getAux()) }
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
