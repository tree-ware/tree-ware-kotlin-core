package org.treeWare.model.codec.decoder.stateMachine

import org.treeWare.common.codec.AbstractDecodingStateMachine
import org.treeWare.common.codec.SkipUnknownStateMachine
import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.getRootMeta
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

        // The root model has a resolved meta-model which does not have the
        // name of the root. The name is in the unresolved meta-model which
        // can be accessed from the main meta-model.
        val mainMeta = root.parent.meta
        val unresolvedRootMeta = mainMeta?.let { getRootMeta(mainMeta) }
        val rootName = unresolvedRootMeta?.let { getMetaName(unresolvedRootMeta) }

        val key = keyName ?: ""
        if (key == rootName) {
            val entityStateMachine =
                BaseEntityStateMachine(false, { root }, stack, auxStateMachineFactory, isWildcardModel)
            stack.addFirst(entityStateMachine)
            return true
        } else if (auxStateMachine != null) {
            val fieldAndAuxNames = getFieldAndAuxNames(key)
            if (fieldAndAuxNames?.fieldName == rootName) {
                // TODO(deepak-nulu): also validate auxName.
                stack.addFirst(auxStateMachine)
                auxStateMachine.newAux()
                return true
            }
        }
        stack.addFirst(SkipUnknownStateMachine<Aux>(stack))
        return true
    }
}
