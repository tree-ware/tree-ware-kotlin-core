package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MutableScalarValueModel
import java.math.BigDecimal

class ScalarValueModelStateMachine(
    private val isListElement: Boolean,
    private val valueFactory: () -> MutableScalarValueModel,
    private val stack: DecodingStack
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var value: MutableScalarValueModel? = null
    private val logger = LogManager.getLogger()

    override fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        assert(value != null)
        value?.setAux(auxName, aux)
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
            val localValue = valueFactory()
            value = localValue
            return localValue.setNullValue()
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            val localValue = valueFactory()
            this.value = localValue
            return localValue.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        try {
            val localValue = valueFactory()
            this.value = localValue
            return localValue.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            val localValue = valueFactory()
            this.value = localValue
            return localValue.setValue(value)
        } finally {
            // Remove self from stack
            stack.pollFirst()
        }
    }
}
