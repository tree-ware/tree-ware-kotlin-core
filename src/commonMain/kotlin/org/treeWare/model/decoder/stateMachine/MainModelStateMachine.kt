package org.treeWare.model.decoder.stateMachine

import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.getMainName
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.util.assertInDevMode

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
        stack.removeFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assertInDevMode(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assertInDevMode(false)
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
        val mainName = getMainName(mainModel)
        if (fieldName != mainName) {
            stack.addFirst(SkipUnknownStateMachine(stack))
            return true
        }
        stack.addFirst(
            BaseEntityStateMachine(
                null,
                { mainModel.getOrNewRoot() },
                stack,
                options,
                errors,
                multiAuxDecodingStateMachineFactory,
                "Root entities must not be null; use empty object {} instead"
            )
        )
        return true
    }
}