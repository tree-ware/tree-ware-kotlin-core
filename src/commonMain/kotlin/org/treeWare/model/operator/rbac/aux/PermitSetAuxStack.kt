package org.treeWare.model.operator.rbac.aux

import org.treeWare.model.operator.set.aux.SetAux

class PermitSetAuxStack {
    /** Return true if any write (CREATE, UPDATE, DELETE) access is permitted. */
    fun isAnySetPermitted(): Boolean = permissionsStack.firstOrNull()?.isAnyWritePermitted() ?: false

    fun isSetPermitted(setAux: SetAux?): Boolean {
        val permissionsAux = permissionsStack.firstOrNull() ?: return false
        return when (setAux) {
            SetAux.CREATE -> permissionsAux.isCreatePermitted()
            SetAux.UPDATE -> permissionsAux.isUpdatePermitted()
            SetAux.DELETE -> permissionsAux.isDeletePermitted()
            null -> false
        }
    }

    fun push(aux: PermissionsAux?) {
        // The current permissions are the passed in `aux` or the parent permissions that are sub-tree scoped.
        val currentPermissions = aux ?: permissionsStack.firstOrNull()?.getSubTreePermissions()
        permissionsStack.addFirst(currentPermissions)
    }

    fun pop() {
        permissionsStack.removeFirst()
    }

    fun peek(): PermissionsAux? = permissionsStack.firstOrNull()

    fun isEmpty(): Boolean = permissionsStack.isEmpty()

    private val permissionsStack = ArrayDeque<PermissionsAux?>()
}