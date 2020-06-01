package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableAssociationValueModel
import org.tree_ware.schema.core.AssociationFieldSchema

class AssociationValueStateMachine<Aux>(
    private val isListElement: Boolean,
    private val associationFactory: () -> MutableAssociationValueModel<Aux>,
    private val schema: AssociationFieldSchema,
    private val stack: DecodingStack,
    private val decodeAux: Boolean
) : AbstractDecodingStateMachine(true) {
    private var association: MutableAssociationValueModel<Aux>? = null
    private val logger = LogManager.getLogger()

    override fun decodeObjectStart(): Boolean {
        association = associationFactory()
        assert(association != null)
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        if (!isListElement) {
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
                stack.addFirst(AssociationPathStateMachine(it.pathKeys, schema.keyEntities, stack, decodeAux))
            }
        } else {
            stack.addFirst(SkipUnknownStateMachine(stack))
        }
        return true
    }

    override fun decodeNullValue(): Boolean {
        return super.decodeNullValue()
    }
}
