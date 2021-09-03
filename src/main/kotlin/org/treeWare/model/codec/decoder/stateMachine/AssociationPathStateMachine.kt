package org.treeWare.model.codec.decoder.stateMachine

import org.treeWare.model.core.MutableEntityKeysModel

class AssociationPathStateMachine<Aux>(
    private val modelList: List<MutableEntityKeysModel<Aux>>,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {
    private var listIndex = 0

    override fun decodeObjectStart(): Boolean {
        if (listIndex >= modelList.size) return false
        val model = modelList[listIndex]
        // `path_keys` is an "internal" structure that will not have aux data, so auxStateMachine is hardcoded to null.
        val entityStateMachine = BaseEntityStateMachine(true, { model }, stack, { null }, false)
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
