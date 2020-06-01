package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.model.core.MutableEntityKeysModel
import org.tree_ware.schema.core.EntitySchema

class AssociationPathStateMachine<Aux>(
    private val modelList: List<MutableEntityKeysModel<Aux>>,
    private val schemaList: List<EntitySchema>,
    private val stack: DecodingStack,
    private val decodeAux: Boolean
) : AbstractDecodingStateMachine(true) {
    private var listIndex = 0

    override fun decodeObjectStart(): Boolean {
        if (listIndex >= modelList.size) return false
        val model = modelList[listIndex]
        val schema = schemaList[listIndex]
        val entityStateMachine = BaseEntityStateMachine(false, { model }, stack, decodeAux)
        // TODO(deepak-nulu): don't call entityStateMachine.decodeObjectStart(). Instead,
        // handle objects in the "path_keys" list the way list fields are handled.
        entityStateMachine.decodeObjectStart()
        stack.addFirst(entityStateMachine)
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
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }
}
