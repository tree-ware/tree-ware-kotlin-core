package org.treeWare.model.codec.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.common.codec.AbstractDecodingStateMachine
import org.treeWare.model.core.MutableScalarFieldModel
import java.math.BigDecimal

class ScalarFieldModelStateMachine<Aux>(
    private val isListElement: Boolean,
    private val fieldFactory: () -> MutableScalarFieldModel<Aux>,
    private val stack: DecodingStack,
    auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private var field: MutableScalarFieldModel<Aux>? = null
    private val logger = LogManager.getLogger()

    override fun setAux(aux: Aux) {
        assert(field != null)
        field?.aux = aux
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

    override fun decodeNullValue(): Boolean {
        try {
            val localField = fieldFactory()
            field = localField
            return localField.setNullValue()
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            val localField = fieldFactory()
            field = localField
            return localField.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            val localField = fieldFactory()
            field = localField
            return localField.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            val localField = fieldFactory()
            field = localField
            return localField.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }
}
