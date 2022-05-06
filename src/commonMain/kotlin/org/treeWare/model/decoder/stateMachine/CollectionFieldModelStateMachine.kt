package org.treeWare.model.decoder.stateMachine

import org.treeWare.model.core.MutableFieldModel
import org.treeWare.util.assertInDevMode

class CollectionFieldModelStateMachine(
    private val collectionFieldModel: MutableFieldModel,
    private val listElementStateMachine: DecodingStateMachine,
    private val stack: DecodingStack,
    private val errors: MutableList<String>
) : ValueDecodingStateMachine, AbstractDecodingStateMachine(true) {
    override fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        collectionFieldModel.setAux(auxName, aux)
    }

    override fun decodeObjectStart(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeObjectEnd(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListStart(): Boolean {
        stack.addFirst(listElementStateMachine)
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.removeFirst()
        return true
    }

    override fun decodeNullValue(): Boolean {
        errors.add("Lists must not be null; use empty array [] instead")
        // Remove self from stack
        stack.removeFirst()
        return false
    }
}