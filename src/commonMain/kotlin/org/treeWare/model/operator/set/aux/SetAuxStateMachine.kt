package org.treeWare.model.operator.set.aux

import org.treeWare.model.decoder.stateMachine.AbstractDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack
import org.treeWare.util.assertInDevMode

class SetAuxStateMachine(
    private val stack: DecodingStack
) : AuxDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var aux: SetAux? = null

    override fun newAux() {
        aux = null
    }

    override fun getAux(): Any? {
        return aux
    }

    override fun decodeStringValue(value: String): Boolean {
        aux = SetAux.valueOf(value.uppercase())
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