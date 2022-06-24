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
}

enum class PermissionScope {
    NONE,
    NODE,
    SUB_TREE
}

fun getPermissionsAux(element: ElementModel?): PermissionsAux? = element?.getAux(PERMISSIONS_AUX_NAME)