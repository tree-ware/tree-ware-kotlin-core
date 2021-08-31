package org.treeWare.model.codec.decoder.stateMachine

import org.apache.logging.log4j.LogManager
import org.treeWare.common.codec.DecodingStateMachine
import org.treeWare.model.core.Model
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved
import java.math.BigDecimal
import java.util.*

typealias DecodingStack = ArrayDeque<DecodingStateMachine>

/**
 * Main state-machine for decoding a model.
 *
 * @param isWildcardModel determines if the first element in a composition-list should be reused
 * instead of creating new elements in the composition-list. It is useful when decoding multiple
 * sources onto the same model instance. Use only when you are sure that there needs to be no
 * more than one element in *every* composition-list.
 */
class ModelDecodingStateMachine<Aux>(
    meta: Model<Resolved>,
    expectedModelType: String,
    auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?,
    isWildcardModel: Boolean = false
) : DecodingStateMachine {
    private val stack = DecodingStack()
    private val modelStateMachine =
        ModelStateMachine(meta, expectedModelType, auxStateMachineFactory, stack, isWildcardModel)
    private val logger = LogManager.getLogger()

    init {
        reinitialize()
    }

    fun reinitialize() {
        stack.clear()
        stack.addFirst(modelStateMachine)
    }

    val model: MutableModel<Aux>? get() = modelStateMachine.model

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
