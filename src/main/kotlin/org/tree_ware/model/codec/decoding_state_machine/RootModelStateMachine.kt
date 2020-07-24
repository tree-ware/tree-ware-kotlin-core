package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.AbstractDecodingStateMachine
import org.tree_ware.common.codec.SkipUnknownStateMachine
import org.tree_ware.model.core.MutableRootModel

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

        if (keyName == root.schema.name) {
            val entityStateMachine =
                BaseEntityStateMachine(false, { root }, stack, auxStateMachineFactory, isWildcardModel)
            if (auxStateMachine == null) stack.addFirst(entityStateMachine)
            else stack.addFirst(ValueAndAuxStateMachine(false, entityStateMachine, auxStateMachineFactory, stack))
        } else {
            stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
        }
        return true
    }
}
