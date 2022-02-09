package org.treeWare.model.decoder.stateMachine

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.ModelDecoderOptions

typealias DecodingStack = ArrayDeque<DecodingStateMachine>

/** Main state-machine for decoding a model. */
class ModelDecodingStateMachine(
    mainMeta: MainModel,
    options: ModelDecoderOptions,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : DecodingStateMachine {
    val mainModel = MutableMainModel(mainMeta)
    val errors = mutableListOf<String>()

    private val stack = DecodingStack()
    private val mainModelStateMachine =
        MainModelStateMachine(mainModel, stack, options, errors, multiAuxDecodingStateMachineFactory)
    private val logger = logging()

    init {
        reinitialize()
    }

    private fun reinitialize() {
        stack.clear()
        stack.addFirst(mainModelStateMachine)
    }

    private fun getTopStateMachine(): DecodingStateMachine? {
        val top = stack.firstOrNull()
        if (top == null) logger.error { "Decoding stack is empty" }
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

    override fun decodeNumericValue(value: String): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeNumericValue(value)
    }

    override fun decodeBooleanValue(value: Boolean): Boolean {
        val top = getTopStateMachine() ?: return false
        return top.decodeBooleanValue(value)
    }
}