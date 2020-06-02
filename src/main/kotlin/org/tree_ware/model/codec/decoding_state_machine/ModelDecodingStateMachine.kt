package org.tree_ware.model.codec.decoding_state_machine

import org.apache.logging.log4j.LogManager
import org.tree_ware.common.codec.DecodingStateMachine
import org.tree_ware.model.core.MutableModel
import java.math.BigDecimal
import java.util.*

typealias DecodingStack = ArrayDeque<DecodingStateMachine>

class ModelDecodingStateMachine<Aux>(private val model: MutableModel<Aux>) : DecodingStateMachine {
    private val stack = DecodingStack()
    private val logger = LogManager.getLogger()

    init {
        stack.addFirst(ModelStateMachine(model, stack))
    }

    private fun getTopStateMachine(): DecodingStateMachine? {
        val top = stack.peekFirst()
        if (top == null) logger.error("Decoding stack is empty")
        return top
    }

    // DecodingStateMachine methods

    override fun decodeObjectStart(): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeObjectStart()
    }

    override fun decodeObjectEnd(): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeObjectEnd()
    }

    override fun decodeListStart(): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeListStart()
    }

    override fun decodeListEnd(): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeListEnd()
    }

    override fun decodeKey(name: String): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeKey(name)
    }

    override fun decodeNullValue(): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeNullValue()
    }

    override fun decodeStringValue(value: String): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeStringValue(value)
    }

    override fun decodeNumericValue(value: BigDecimal): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeNumericValue(value)
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeBooleanValue(value)
    }
}
