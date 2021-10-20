package org.treeWare.model.decoder.stateMachine

import org.treeWare.model.core.MutableFieldModel

class CollectionFieldModelStateMachine(
    private val collectionFieldModel: MutableFieldModel,
    private val listElementStateMachine: DecodingStateMachine,
    private val stack: DecodingStack
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    override fun setAux(auxType: String, aux: Any?) {
        if (aux == null) return
        collectionFieldModel.setAux(auxType, aux)
    }

    override fun decodeObjectStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        stack.addFirst(listElementStateMachine)
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }
}
