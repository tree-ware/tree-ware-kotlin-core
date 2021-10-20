package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MutablePassword2wayModel
import java.math.BigDecimal

class Password2wayModelStateMachine(
    private val isListElement: Boolean,
    private val passwordFactory: () -> MutablePassword2wayModel,
    private val stack: DecodingStack,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine?
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var auxStateMachine: AuxDecodingStateMachine? = null
    private var password2way: MutablePassword2wayModel? = null
    private val logger = LogManager.getLogger()

    override fun setAux(auxType: String, aux: Any?) {
        if (aux == null) return
        assert(password2way != null)
        password2way?.setAux(auxType, aux)
    }

    override fun decodeObjectStart(): Boolean {
        password2way = passwordFactory()
        assert(password2way != null)
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
                logger.error("No parent decoding state machine for password2way")
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
            "unencrypted" -> password2way?.setUnencrypted(value)
            "encrypted" -> {
                password2way?.encrypted = value
                password2way?.unencrypted = null
            }
        }
        return true
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        super.decodeNumericValue(value)
        if (keyName == "cipher_version") password2way?.cipherVersion = value.toInt()
        return true
    }
}
