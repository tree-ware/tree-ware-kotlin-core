package org.treeWare.model.codec.decoder.stateMachine

import org.treeWare.model.core.Model
import org.treeWare.model.core.MutableModel
import org.treeWare.model.core.Resolved

class ModelStateMachine<Aux>(
    private val meta: Model<Resolved>,
    private val expectedModelType: String,
    private val auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?,
    private val stack: DecodingStack,
    private val isWildcardModel: Boolean
) : AbstractDecodingStateMachine(true) {
    var model: MutableModel<Aux>? = null
        private set

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
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

    override fun decodeKey(name: String): Boolean {
        super.decodeKey(name)

        if (keyName != expectedModelType) return false
        if (model == null) model = MutableModel(meta)
        model?.also { decodeModel(expectedModelType, it) { auxStateMachineFactory(stack) } }
        return true
    }

    private fun decodeModel(
        modelType: String,
        newModel: MutableModel<Aux>,
        auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
    ) {
        newModel.type = modelType
        val root = newModel.getOrNewRoot()
        stack.addFirst(RootModelStateMachine(root, stack, auxStateMachineFactory, isWildcardModel))
    }
}
