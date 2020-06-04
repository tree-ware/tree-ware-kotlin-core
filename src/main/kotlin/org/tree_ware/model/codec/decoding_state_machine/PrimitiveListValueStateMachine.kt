package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.DecodingStateMachine
import org.tree_ware.model.core.MutableListFieldModel
import java.math.BigDecimal

class PrimitiveListValueStateMachine<Aux>(
    private val field: MutableListFieldModel<Aux>,
    private val stack: DecodingStack,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private val auxStateMachine = auxStateMachineFactory()
    private val logger = LogManager.getLogger()

    override fun setAux(aux: Aux) {
        field.aux = aux
    }

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
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        if (auxStateMachine != null) {
            // This method should never get called
            assert(false)
            return false
        }

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
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            return field.addValue(value)
        } finally {
            if (auxStateMachine != null) {
                // Remove self from stack
                stack.pollFirst()
            }
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            return field.addValue(value)
        } finally {
            if (auxStateMachine != null) {
                // Remove self from stack
                stack.pollFirst()
            }
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            return field.addValue(value)
        } finally {
            if (auxStateMachine != null) {
                // Remove self from stack
                stack.pollFirst()
            }
        }
    }
}
