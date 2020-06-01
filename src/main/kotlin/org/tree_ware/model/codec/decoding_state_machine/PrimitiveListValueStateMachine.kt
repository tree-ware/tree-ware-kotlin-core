package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.model.core.MutableListFieldModel
import java.math.BigDecimal

class PrimitiveListValueStateMachine<Aux>(
    private val field: MutableListFieldModel<Aux>,
    private val stack: DecodingStack,
    private val decodeAux: Boolean
) : AbstractDecodingStateMachine(true) {
    private val logger = LogManager.getLogger()

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
        if (decodeAux) {
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
            if (decodeAux) {
                // Remove self from stack
                stack.pollFirst()
            }
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            return field.addValue(value)
        } finally {
            if (decodeAux) {
                // Remove self from stack
                stack.pollFirst()
            }
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            return field.addValue(value)
        } finally {
            if (decodeAux) {
                // Remove self from stack
                stack.pollFirst()
            }
        }
    }
}
