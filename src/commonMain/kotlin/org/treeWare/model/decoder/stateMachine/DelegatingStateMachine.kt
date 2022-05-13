package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.logging

typealias DecodingStack = ArrayDeque<DecodingStateMachine>

private val logger = logging()

/** Main state-machine for decoding a model. */
class DelegatingStateMachine(
    private val debug: Boolean = false,
    initialStateMachineFactory: (errors: MutableList<String>, stack: DecodingStack) -> DecodingStateMachine
) : DecodingStateMachine {
    val errors = mutableListOf<String>()
    private val stack = DecodingStack()

    init {
        stack.addFirst(initialStateMachineFactory(errors, stack))
    }

    private fun getTopStateMachine(): DecodingStateMachine? {
        val top = stack.firstOrNull()
        if (top == null) logger.error { "Decoding stack is empty" }
        return top
    }

    // DecodingStateMachine methods

    override fun decodeObjectStart(): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode object start with $top" }
        if (top == null) return false
        return top.decodeObjectStart()
    }

    override fun decodeObjectEnd(): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode object end with $top" }
        if (top == null) return false
        return top.decodeObjectEnd()
    }

    override fun decodeListStart(): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode list start with $top" }
        if (top == null) return false
        return top.decodeListStart()
    }

    override fun decodeListEnd(): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode list end with $top" }
        if (top == null) return false
        return top.decodeListEnd()
    }

    override fun decodeKey(name: String): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode key $name with $top" }
        if (top == null) return false
        return top.decodeKey(name)
    }

    override fun decodeNullValue(): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode null value with $top" }
        if (top == null) return false
        return top.decodeNullValue()
    }

    override fun decodeStringValue(value: String): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode string value $value with $top" }
        if (top == null) return false
        return top.decodeStringValue(value)
    }

    override fun decodeNumericValue(value: String): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode numeric value $value with $top" }
        if (top == null) return false
        return top.decodeNumericValue(value)
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        val top = getTopStateMachine()
        if (debug) logger.info { "decode boolean value $value with $top" }
        if (top == null) return false
        return top.decodeBooleanValue(value)
    }
}