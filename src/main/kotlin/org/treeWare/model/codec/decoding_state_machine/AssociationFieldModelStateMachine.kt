package org.treeWare.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.treeWare.common.codec.AbstractDecodingStateMachine
import org.treeWare.common.codec.SkipUnknownStateMachine
import org.treeWare.model.core.MutableAssociationFieldModel
import org.treeWare.schema.core.AssociationFieldSchema

class AssociationFieldModelStateMachine<Aux>(
    private val isListElement: Boolean,
    private val associationFactory: () -> MutableAssociationFieldModel<Aux>,
    private val schema: AssociationFieldSchema,
    private val stack: DecodingStack,
    private val auxStateMachine: AuxDecodingStateMachine<Aux>?
) : ValueDecodingStateMachine<Aux>, AbstractDecodingStateMachine(true) {
    private var association: MutableAssociationFieldModel<Aux>? = null
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
        if (auxStateMachine != null || !isListElement) {
            // Remove self from stack
            stack.pollFirst()
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

        if (keyName == "path_keys") {
            association?.also {
                stack.addFirst(AssociationPathStateMachine(it.newValue(), schema.keyEntities, stack))
            }
        } else {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
        }
        return true
    }

    override fun decodeNullValue(): Boolean {
        association = associationFactory()
        assert(association != null)
        return association?.setNullValue() ?: false
    }
}
