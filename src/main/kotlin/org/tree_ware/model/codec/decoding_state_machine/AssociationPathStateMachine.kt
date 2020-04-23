package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.schema.core.EntitySchema
import org.tree_ware.model.core.MutableEntityKeysModel

class AssociationPathStateMachine(
    private val modelList: List<MutableEntityKeysModel>,
    private val schemaList: List<EntitySchema>,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    private var listIndex = 0

    override fun decodeObjectStart(): Boolean {
        if (listIndex >= modelList.size) return false
        val model = modelList[listIndex]
        val schema = schemaList[listIndex]
        stack.addFirst(BaseEntityStateMachine(model, schema, stack))
        // TODO: validate that all keys and only keys have been decoded
        ++listIndex
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        stack.addFirst(SkipUnknownStateMachine(stack))
        resetKeyState()
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }
}
