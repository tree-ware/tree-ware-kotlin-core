package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine

class ErrorAuxStateMachine : AuxDecodingStateMachine<String>, AbstractDecodingStateMachine(true) {
    private var error: String? = null

    override fun newAux() {
        error = null
    }

    override fun getAux(): String? {
        return error
    }

    override fun decodeStringValue(value: String): Boolean {
        error = value
        return true
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
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }
}
