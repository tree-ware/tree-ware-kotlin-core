package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MutableScalarValueModel
import org.treeWare.util.assertInDevMode

class ScalarValueModelStateMachine(
    private val isListElement: Boolean,
    private val valueFactory: () -> MutableScalarValueModel,
    private val stack: DecodingStack
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var value: MutableScalarValueModel? = null
    private val logger = logging()

    override fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        assertInDevMode(value != null)
        value?.setAux(auxName, aux)
    }

    override fun decodeObjectStart(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        if (isListElement) {
            // End of the list needs to be handled by parent state machine.
            // So remove self from stack and call decodeListEnd() on previous
            // state machine.
            stack.removeFirst()
            val parentStateMachine = stack.firstOrNull()
            if (parentStateMachine == null) {
                logger.error { "No parent decoding state machine" }
                return false
            }
            return parentStateMachine.decodeListEnd()
        } else {
            // This method should never get called
            assertInDevMode(false)
            return false
        }
    }

    override fun decodeNullValue(): Boolean {
        value = null
        stack.removeFirst()
        return true
    }

    override fun decodeStringValue(value: String): Boolean {
        try {
            val localValue = valueFactory()
            this.value = localValue
            return localValue.setValue(value)
        } finally {
            // Remove self from stack
            stack.removeFirst()
        }
    }

    override fun decodeNumericValue(value: String): Boolean {
        try {
            val localValue = valueFactory()
            this.value = localValue
            return localValue.setValue(value)
        } finally {
            // Remove self from stack
            stack.removeFirst()
        }
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        try {
            val localValue = valueFactory()
            this.value = localValue
            return localValue.setValue(value)
        } finally {
            // Remove self from stack
            stack.removeFirst()
        }
    }
}