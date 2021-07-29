package org.treeWare.model.codec.decoder.stateMachine

import org.treeWare.common.codec.AbstractDecodingStateMachine
import org.treeWare.common.codec.SkipUnknownStateMachine
import org.treeWare.model.core.MutableRootModel

class RootModelStateMachine<Aux>(
    private val root: MutableRootModel<Aux>,
    private val stack: DecodingStack,
    private val auxStateMachineFactory: () -> AuxDecodingStateMachine<Aux>?,
    private val isWildcardModel: Boolean
) : AbstractDecodingStateMachine(true) {
    private val auxStateMachine = auxStateMachineFactory()

    override fun decodeObjectStart(): Boolean {
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // TODO(deepak-nulu): set aux when found rather than on the way out.
        auxStateMachine?.getAux()?.also { root.aux = it }
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
        if (key == root.schema.name) {
            val entityStateMachine =
                BaseEntityStateMachine(false, { root }, stack, auxStateMachineFactory, isWildcardModel)
            stack.addFirst(entityStateMachine)
            return true
        } else if (auxStateMachine != null) {
            val fieldAndAuxNames = getFieldAndAuxNames(key)
            if (fieldAndAuxNames?.fieldName == root.schema.name) {
                // TODO(deepak-nulu): also validate auxName.
                stack.addFirst(auxStateMachine)
                return true
            }
        }
        stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
        return true
    }
}
