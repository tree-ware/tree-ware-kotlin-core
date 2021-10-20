package org.treeWare.model.decoder.stateMachine

import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.getRootMeta
import org.treeWare.model.core.MutableRootModel
import org.treeWare.model.decoder.ModelDecoderOptions

class RootModelStateMachine(
    private val root: MutableRootModel,
    private val stack: DecodingStack,
    private val options: ModelDecoderOptions,
    private val errors: MutableList<String>,
    private val multiAuxDecodingStateMachineFactory: MultiAuxDecodingStateMachineFactory
) : AbstractDecodingStateMachine(true) {
    private val auxStateMachines = LinkedHashMap<String, AuxDecodingStateMachine>()

    private fun setAux(auxName: String, aux: Any?) {
        if (aux == null) return
        root.setAux(auxName, aux)
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

        // The root model has a resolved meta-model which does not have the
        // name of the root. The name is in the unresolved meta-model which
        // can be accessed from the main meta-model.
        val mainMeta = root.parent.meta
        val unresolvedRootMeta = mainMeta?.let { getRootMeta(mainMeta) }
        val rootName = unresolvedRootMeta?.let { getMetaName(unresolvedRootMeta) }

        val key = keyName ?: ""
        if (key == rootName) {
            val entityStateMachine =
                BaseEntityStateMachine(
                    false,
                    null,
                    { root },
                    stack,
                    options,
                    errors,
                    multiAuxDecodingStateMachineFactory
                )
            stack.addFirst(entityStateMachine)
            return true
        } else {
            getFieldAndAuxNames(key)?.also { (fieldName, auxName) ->
                if (fieldName == rootName && auxName != null) {
                    val auxStateMachine = multiAuxDecodingStateMachineFactory.newAuxDecodingStateMachine(auxName, stack)
                    if (auxStateMachine != null) {
                        stack.addFirst(auxStateMachine)
                        auxStateMachine.newAux()
                        auxStateMachines[auxName] = auxStateMachine
                        return true
                    }
                }
            }
        }
        stack.addFirst(SkipUnknownStateMachine(stack))
        return true
    }
}
