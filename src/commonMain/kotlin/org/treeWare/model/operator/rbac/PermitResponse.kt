package org.treeWare.model.operator.rbac

import org.treeWare.model.core.MutableEntityModel

sealed interface PermitResponse

data class FullyPermitted(val permitted: MutableEntityModel) : PermitResponse

data class PartiallyPermitted(val permitted: MutableEntityModel) : PermitResponse

object NotPermitted : PermitResponse