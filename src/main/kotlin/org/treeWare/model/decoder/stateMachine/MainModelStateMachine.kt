package org.treeWare.model.decoder.stateMachine

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.Resolved
import org.treeWare.model.decoder.ModelDecoderOptions

class MainModelStateMachine<Aux>(
    private val meta: MainModel<Resolved>,
    private val expectedModelType: String,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val auxStateMachineFactory: (stack: DecodingStack) -> AuxDecodingStateMachine<Aux>?
) : AbstractDecodingStateMachine(true) {
    var mainModel: MutableMainModel<Aux>? = null
        private set
    val errors = mutableListOf<String>()

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
        if (mainModel == null) mainModel = MutableMainModel(meta)
        mainModel?.also { decodeModel(expectedModelType, it) { auxStateMachineFactory(stack) } }
        return true
    }

    private fun decodeModel(
        modelType: String,
        newMain: MutableMainModel<Aux>,
        auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?
    ) {
        newMain.type = modelType
        val root = newMain.getOrNewRoot()
        stack.addFirst(RootModelStateMachine(root, stack, options, errors, auxStateMachineFactory))
    }
}
