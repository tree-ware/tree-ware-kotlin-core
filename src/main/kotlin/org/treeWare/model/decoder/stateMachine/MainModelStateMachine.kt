package org.treeWare.model.decoder.stateMachine

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.ModelDecoderOptions

class MainModelStateMachine(
    private val meta: MainModel,
    private val expectedModelType: String,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : AbstractDecodingStateMachine(true) {
    var mainModel: MutableMainModel? = null
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
        mainModel?.also { decodeModel(it, multiAuxDecodingStateMachineFactory) }
        return true
    }

    private fun decodeModel(
        newMain: MutableMainModel,
        multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
    ) {
        val root = newMain.getOrNewRoot()
        stack.addFirst(RootModelStateMachine(root, stack, options, errors, multiAuxDecodingStateMachineFactory))
    }
}
