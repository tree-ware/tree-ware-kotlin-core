package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MutableAssociationModel
import org.treeWare.model.decoder.ModelDecoderOptions

class AssociationModelStateMachine(
    private val isListElement: Boolean,
    private val associationFactory: () -> MutableAssociationModel,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine?
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var auxStateMachine: AuxDecodingStateMachine? = null
    private var association: MutableAssociationModel? = null
    private val logger = LogManager.getLogger()

    override fun setAux(auxType: String, aux: Any?) {
        if (aux == null) return
        assert(association != null)
        association?.setAux(auxType, aux)
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
                logger.error("No parent decoding state machine for association")
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
        val (fieldName, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = auxStateMachineFactory()
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                this.auxStateMachine = auxStateMachine
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
        assert(association != null)
        return association?.setNullValue() ?: false
    }
}
