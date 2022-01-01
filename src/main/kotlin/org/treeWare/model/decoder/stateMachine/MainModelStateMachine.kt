package org.treeWare.model.decoder.stateMachine

import org.treeWare.metaModel.getRootMetaName
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.ModelDecoderOptions

class MainModelStateMachine(
    private val mainModel: MutableMainModel,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : AbstractDecodingStateMachine(true) {
    private val auxStateMachines = LinkedHashMap<String, AuxDecodingStateMachine>()

    private fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        mainModel.setAux(auxName, aux)
    }

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // TODO(deepak-nulu): set aux when found rather than on the way out.
        auxStateMachines.forEach { (auxName, auxStateMachine) ->
            setAux(auxName, auxStateMachine.getAux())
        }
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

        val key = keyName ?: ""
        val fieldAndAuxNames = getFieldAndAuxNames(key)
        if (fieldAndAuxNames == null) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        val (fieldName, auxName) = fieldAndAuxNames
        if (auxName != null) {
            val auxStateMachine = multiAuxDecodingStateMachineFactory.newAuxDecodingStateMachine(auxName, stack)
            if (auxStateMachine != null) {
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                auxStateMachines[auxName] = auxStateMachine
            } else stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        val rootName = mainModel.mainMeta?.let { getRootMetaName(it) }
        if (fieldName != rootName) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        stack.addFirst(
            BaseEntityStateMachine(
                false,
                null,
                { mainModel.getOrNewRoot() },
                stack,
                options,
                errors,
                multiAuxDecodingStateMachineFactory
            )
        )
        return true
    }
}