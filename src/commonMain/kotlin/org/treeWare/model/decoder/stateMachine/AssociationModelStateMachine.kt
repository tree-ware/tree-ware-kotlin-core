package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.KmLog
import org.treeWare.model.core.MutableAssociationModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.util.assertInDevMode

class AssociationModelStateMachine(
    private val isListElement: Boolean,
    private val associationFactory: () -> MutableAssociationModel,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private val auxStateMachines = LinkedHashMap<String, AuxDecodingStateMachine>()
    private var association: MutableAssociationModel? = null
    private val logger = KmLog()

    override fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        assertInDevMode(association != null)
        association?.setAux(auxName, aux)
    }

    override fun decodeObjectStart(): Boolean {
        association = associationFactory()
        assertInDevMode(association != null)
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
            // This state-machine instance gets reused in lists, so clear the map.
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
                logger.error { "No parent decoding state machine for association" }
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
        val (fieldName, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = multiAuxDecodingStateMachineFactory.newAuxDecodingStateMachine(auxName, stack)
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                auxStateMachines[auxName] = auxStateMachine
            } else stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        if (fieldName == "path_keys") {
            association?.also {
                stack.addFirst(AssociationPathStateMachine(it.newValue(), stack, options, errors))
            }
            return true
        }
        stack.addFirst(SkipUnknownStateMachine(stack))
        return true
    }

    override fun decodeNullValue(): Boolean {
        association = associationFactory()
        assertInDevMode(association != null)
        return association?.setNullValue() ?: false
    }
}
