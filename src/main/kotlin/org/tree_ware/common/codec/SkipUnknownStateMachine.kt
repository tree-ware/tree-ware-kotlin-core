package org.tree_ware.common.codec

import org.tree_ware.model.codec.decoding_state_machine.DecodingStack
import java.math.BigDecimal

class SkipUnknownStateMachine(private val stack: DecodingStack) : AbstractDecodingStateMachine(true) {
    private var depth = 0

    private fun popIfDone() {
        if (depth == 0) {
            // Remove self from stack
            stack.pollFirst()
        }
    }

    override fun decodeObjectStart(): Boolean {
        ++depth
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        --depth
        popIfDone()
        return true
    }

    override fun decodeListStart(): Boolean {
        ++depth
        return true
    }

    override fun decodeListEnd(): Boolean {
        --depth
        popIfDone()
        return true
    }

    override fun decodeKey(name: String): Boolean {
        return true
    }

    override fun decodeNullValue(): Boolean {
        popIfDone()
        return true
    }

    override fun decodeStringValue(value: String): Boolean {
        popIfDone()
        return true
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        popIfDone()
        return true
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        popIfDone()
        return true
    }
}
