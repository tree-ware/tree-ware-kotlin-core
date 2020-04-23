package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.DecodingStateMachine
import org.tree_ware.model.core.MutableModel
import java.math.BigDecimal
import java.util.*

typealias DecodingStack = ArrayDeque<DecodingStateMachine>

class ModelDecodingStateMachine(private val model: MutableModel) : DecodingStateMachine {
    private var stack = DecodingStack()
    private var firstTime = true
    private val logger = LogManager.getLogger()

    private fun getTopStateMachine(): DecodingStateMachine? {
        val top = stack.peekFirst()
        if (top == null) logger.error("Decoding stack is empty")
        return top
    }

    // DecodingStateMachine methods

    override fun decodeObjectStart(): Boolean {
        if (firstTime) {
            firstTime = false
            stack.addFirst(ModelStateMachine(model, stack))
            return true
        } else {
            val top = getTopStateMachine() ?: return false
            return top.decodeObjectStart()
        }
    }

    override fun decodeObjectEnd(): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeObjectEnd()
    }

    override fun decodeListStart(): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeListStart()
    }

    override fun decodeListEnd(): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeListEnd()
    }

    override fun decodeKey(name: String): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeKey(name)
    }

    override fun decodeStringValue(value: String): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeStringValue(value)
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeNumericValue(value)
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        if (firstTime) return false
        val top = getTopStateMachine() ?: return false
        return top.decodeBooleanValue(value)
    }
}
