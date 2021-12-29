package org.treeWare.model.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.ModelDecoderOptions
import java.math.BigDecimal
import java.util.*

typealias DecodingStack = ArrayDeque<DecodingStateMachine>

/** Main state-machine for decoding a model. */
class ModelDecodingStateMachine(
    meta: MainModel,
    options: ModelDecoderOptions,
    multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : DecodingStateMachine {
    val mainModel = MutableMainModel(meta)
    val errors = mutableListOf<String>()

    private val stack = DecodingStack()
    private val rootModelStateMachine =
        RootModelStateMachine(mainModel.getOrNewRoot(), stack, options, errors, multiAuxDecodingStateMachineFactory)
    private val logger = LogManager.getLogger()

    init {
        reinitialize()
    }

    private fun reinitialize() {
        stack.clear()
        stack.addFirst(rootModelStateMachine)
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
