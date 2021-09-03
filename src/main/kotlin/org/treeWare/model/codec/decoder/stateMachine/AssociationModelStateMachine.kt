package org.treeWare.model.codec.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MutableAssociationModel

class AssociationModelStateMachine<Aux>(
    private val isListElement: Boolean,
    private val associationFactory: () -> MutableAssociationModel<Aux>,
    private val stack: DecodingStack,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private var auxStateMachine: AuxDecodingStateMachine<Aux>? = null
    private var association: MutableAssociationModel<Aux>? = null
    private val logger = LogManager.getLogger()

    override fun setAux(aux: Aux) {
        assert(association != null)
        association?.aux = aux
    }

    override fun decodeObjectStart(): Boolean {
        association = associationFactory()
        assert(association != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (!isListElement) {
            // Remove self from stack
            stack.pollFirst()
        } else {
            auxStateMachine?.getAux()?.also { association?.aux = it }
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

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        val key = keyName ?: ""
        val fieldAndAuxNames = getFieldAndAuxNames(key)
        if (fieldAndAuxNames == null) {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        val (fieldName, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = auxStateMachineFactory()
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                this.auxStateMachine = auxStateMachine
            } else stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
            return true
        }
        if (fieldName == "path_keys") {
            association?.also {
                stack.addFirst(AssociationPathStateMachine(it.newValue(), stack))
            }
            return true
        }
        stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
        return true
    }

    override fun decodeNullValue(): Boolean {
        association = associationFactory()
        assert(association != null)
        return association?.setNullValue() ?: false
    }
}
