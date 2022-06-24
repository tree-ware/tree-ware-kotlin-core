package org.treeWare.model.operator.rbac.aux

class PermitGetAuxStack {
    fun isGetPermitted(): Boolean = readScopeStack.firstOrNull()?.let { it != PermissionScope.NONE } ?: false

    fun push(aux: PermissionsAux?) {
        val readScope = if (aux == null) {
            val parentScope = readScopeStack.firstOrNull()
            parentScope.takeIf { it == PermissionScope.SUB_TREE }
        } else aux.getReadScope()
        readScopeStack.addFirst(readScope)
    }

    fun pop() {
        readScopeStack.removeFirst()
    }

    fun isEmpty(): Boolean = readScopeStack.isEmpty()

    private val readScopeStack = ArrayDeque<PermissionScope?>()
}