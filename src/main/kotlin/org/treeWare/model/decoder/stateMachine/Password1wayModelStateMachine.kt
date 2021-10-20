package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MutablePassword1wayModel
import java.math.BigDecimal

class Password1wayModelStateMachine(
    private val isListElement: Boolean,
    private val passwordFactory: () -> MutablePassword1wayModel,
    private val stack: DecodingStack,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine?
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var auxStateMachine: AuxDecodingStateMachine? = null
    private var password1way: MutablePassword1wayModel? = null
    private val logger = LogManager.getLogger()

    override fun setAux(auxType: String, aux: Any?) {
        if (aux == null) return
        assert(password1way != null)
        password1way?.setAux(auxType, aux)
    }

    override fun decodeObjectStart(): Boolean {
        password1way = passwordFactory()
        assert(password1way != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (!isListElement) {
            // Remove self from stack
            stack.pollFirst()
        } else {
            auxStateMachine?.also { setAux(it.auxType, it.getAux()) }
            // This state-machine instance gets reused in lists, so clear it.
            auxStateMachine = null
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
                logger.error("No parent decoding state machine for password1way")
                return false
            }
            return parentStateMachine.decodeListEnd()
        } else {
            // This method should never get called
            assert(false)
            return false
        }
    }

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        val key = keyName ?: ""
        val fieldAndAuxNames = getFieldAndAuxNames(key)
        if (fieldAndAuxNames == null) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        val (_, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = auxStateMachineFactory()
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                this.auxStateMachine = auxStateMachine
            } else stack.addFirst(SkipUnknownStateMachine(stack))
        }
        return true
    }

    override fun decodeStringValue(value: String): Boolean {
        super.decodeStringValue(value)
        when (keyName) {
            "unhashed" -> password1way?.setUnhashed(value)
            "hashed" -> {
                password1way?.hashed = value
                password1way?.unhashed = null
            }
        }
        return true
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        super.decodeNumericValue(value)
        if (keyName == "hash_version") password1way?.hashVersion = value.toInt()
        return true
    }
}
