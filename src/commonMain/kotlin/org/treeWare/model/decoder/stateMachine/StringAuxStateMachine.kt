package org.treeWare.model.decoder.stateMachine

import org.treeWare.util.assertInDevMode

class StringAuxStateMachine(
    private val stack: DecodingStack
) : AuxDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var aux: String? = null

    override fun newAux() {
        aux = null
    }

    override fun getAux(): String? {
        return aux
    }

    override fun decodeStringValue(value: String): Boolean {
        aux = value
        // Remove self from stack
        stack.removeFirst()
        return true
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
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }
}
