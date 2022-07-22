package org.treeWare.model.decoder.stateMachine

import org.treeWare.util.assertInDevMode

class StringListStateMachine(
    private val list: MutableList<String>,
    private val stack: DecodingStack
) : AbstractDecodingStateMachine(true) {

    override fun decodeStringValue(value: String): Boolean {
        list.add(value)
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
        return true
    }

    override fun decodeListEnd(): Boolean {
        // Remove self from stack
        stack.removeFirst()
        return true
    }
}