package org.treeWare.model.operator.set.aux

import org.treeWare.metaModel.Granularity
import org.treeWare.util.assertInDevMode

class SetAuxStack {
    var nothingToSet = true
        private set

    fun peekActive(): SetAux? = activeSetAuxStack.firstOrNull()
    fun isEmpty(): Boolean = currentSetAuxStack.isEmpty()

    /**
     * Pushes the specified SetAux value onto the stack.
     *
     * @return an error message if the aux value is not valid compared to the active aux.
     */
    fun push(newAux: SetAux?, isEntity: Boolean = false, granularity: Granularity? = null): String? =
        if (granularity == Granularity.SUB_TREE && newAux != null) {
            pushAux(null)
            "set_ aux is not valid inside a sub-tree with sub_tree granularity"
        } else when (val activeAux = peekActive()) {
            SetAux.CREATE -> {
                if (newAux == null) {
                    pushAux(null)
                    null
                } else if (newAux != SetAux.CREATE) {
                    pushAux(null)
                    getAuxError(activeAux, newAux)
                } else {
                    pushAux(newAux)
                    null
                }
            }
            SetAux.DELETE -> {
                if (newAux == null) {
                    pushAux(null)
                    if (!isEntity || granularity == Granularity.SUB_TREE) null
                    else "entity without `delete` must not be in the subtree of a `delete`"
                } else if (newAux != SetAux.DELETE) {
                    pushAux(null)
                    getAuxError(activeAux, newAux)
                } else {
                    pushAux(newAux)
                    null
                }
            }
            else -> {
                pushAux(newAux)
                null
            }
        }

    private fun pushAux(aux: SetAux?) {
        currentSetAuxStack.addFirst(aux)
        aux?.also {
            nothingToSet = false
            activeSetAuxStack.addFirst(it)
        }
    }

    fun pop() {
        val setAux = currentSetAuxStack.removeFirst()
        setAux?.also {
            val active = activeSetAuxStack.removeFirst()
            assertInDevMode(active == it)
        }
    }

    private fun getAuxError(activeAux: SetAux, newAux: SetAux): String {
        val active = activeAux.name.lowercase()
        val new = newAux.name.lowercase()
        return "`$new` must not be in the subtree of a `$active`"
    }

    private val currentSetAuxStack = ArrayDeque<SetAux?>()

    /**
     * A stack of active SetAux values.
     * The active SetAux value for an element is its own SetAux value or the SetAux value from its nearest ancestor.
     * The top of this stack represents the active SetAux value.
     */
    private val activeSetAuxStack = ArrayDeque<SetAux>()
}