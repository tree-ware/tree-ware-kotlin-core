package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MutablePassword2wayModel
import org.treeWare.util.assertInDevMode

class Password2wayModelStateMachine(
    private val isListElement: Boolean,
    private val passwordFactory: () -> MutablePassword2wayModel,
    private val stack: DecodingStack,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private val auxStateMachines = LinkedHashMap<String, AuxDecodingStateMachine>()
    private var password2way: MutablePassword2wayModel? = null
    private val logger = logging()

    override fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        assertInDevMode(password2way != null)
        password2way?.setAux(auxName, aux)
    }

    override fun decodeObjectStart(): Boolean {
        password2way = passwordFactory()
        assertInDevMode(password2way != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (!isListElement) {
            // Remove self from stack
            stack.removeFirst()
        } else {
            auxStateMachines.forEach { (auxName, auxStateMachine) ->
                setAux(auxName, auxStateMachine.getAux())
            }
            // This state-machine instance gets reused in lists, so clear it.
            auxStateMachines.clear()
        }
        return true
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
                logger.error { "No parent decoding state machine for password2way" }
                return false
            }
            return parentStateMachine.decodeListEnd()
        } else {
            // This method should never get called
            assertInDevMode(false)
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
            val auxStateMachine = multiAuxDecodingStateMachineFactory.newAuxDecodingStateMachine(auxName, stack)
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                auxStateMachines[auxName] = auxStateMachine
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

    override fun decodeNumericValue(value: String): Boolean {
        super.decodeNumericValue(value)
        if (keyName == "cipher_version") password2way?.cipherVersion = value.toInt()
        return true
    }
}