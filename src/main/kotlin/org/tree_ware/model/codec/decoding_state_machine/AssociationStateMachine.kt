package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.schema.core.AssociationFieldSchema
import org.tree_ware.model.core.MutableAssociationValueModel

class AssociationStateMachine(
    private val association: MutableAssociationValueModel,
    private val schema: AssociationFieldSchema,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    override fun decodeObjectStart(): Boolean {
        stack.addFirst(SkipUnknownStateMachine(stack))
        resetKeyState()
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        if (keyName == "path_keys") {
            stack.addFirst(AssociationPathStateMachine(association.pathKeys, schema.keyEntities, stack))
        } else {
            stack.addFirst(SkipUnknownStateMachine(stack))
        }
        resetKeyState()
        return true
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }
}
