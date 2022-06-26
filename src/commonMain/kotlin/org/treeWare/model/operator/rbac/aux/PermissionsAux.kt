package org.treeWare.model.operator.rbac.aux

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

const val PERMISSIONS_AUX_NAME = "permissions"

data class PermissionsAux(
    val create: PermissionScope? = null,
    val read: PermissionScope? = null,
    val update: PermissionScope? = null,
    val delete: PermissionScope? = null,
    val crud: PermissionScope? = null,
    val all: PermissionScope? = null,
) {
    fun getReadScope(): PermissionScope? = read ?: crud ?: all

    fun isCreatePermitted(): Boolean = (create ?: crud ?: all)?.let { it != PermissionScope.NONE } ?: false
    fun isUpdatePermitted(): Boolean = (update ?: crud ?: all)?.let { it != PermissionScope.NONE } ?: false
    fun isDeletePermitted(): Boolean = (delete ?: crud ?: all)?.let { it != PermissionScope.NONE } ?: false

    /** Return true if any write (CREATE, UPDATE, DELETE) access is permitted. */
    fun isAnyWritePermitted(): Boolean = isCreatePermitted() || isUpdatePermitted() || isDeletePermitted()

    /** Return sub-tree scoped permissions in a new instance. */
    fun getSubTreePermissions(): PermissionsAux = PermissionsAux(
        create = create.takeIf { it == PermissionScope.SUB_TREE },
        read = read.takeIf { it == PermissionScope.SUB_TREE },
        update = update.takeIf { it == PermissionScope.SUB_TREE },
        delete = delete.takeIf { it == PermissionScope.SUB_TREE },
        crud = crud.takeIf { it == PermissionScope.SUB_TREE },
        all = all.takeIf { it == PermissionScope.SUB_TREE },
    )
}

enum class PermissionScope {
    NONE,
    NODE,
    SUB_TREE
}

fun getPermissionsAux(element: ElementModel?): PermissionsAux? = element?.getAux(PERMISSIONS_AUX_NAME)