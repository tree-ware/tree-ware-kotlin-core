package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager

const val VALUE_KEY = "value"

class ValueAndAuxStateMachine(
    private val isListElement: Boolean,
    private val valueStateMachine: ValueDecodingStateMachine,
    private val stack: DecodingStack,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : AbstractDecodingStateMachine(true) {
    private val auxStateMachines = LinkedHashMap<String, AuxDecodingStateMachine>()
    private val logger = LogManager.getLogger()

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        val key = keyName ?: ""
        if (key == VALUE_KEY) stack.addFirst(valueStateMachine)
        else getFieldAndAuxNames(key)?.also { (_, auxName) ->
            val auxStateMachine = auxName?.let {
                multiAuxDecodingStateMachineFactory.newAuxDecodingStateMachine(it, stack)
            }
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.decodeKey(name)
                auxStateMachine.newAux()
                auxStateMachines[auxName] = auxStateMachine
            }
        }
        return true
    }

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        auxStateMachines.forEach { (auxName, auxStateMachine) ->
            valueStateMachine.setAux(auxName, auxStateMachine.getAux())
        }
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
