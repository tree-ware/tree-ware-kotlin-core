package org.treeWare.model.operator.rbac

import org.treeWare.model.core.MutableMainModel

sealed interface PermitResponse

data class FullyPermitted(val permitted: MutableMainModel) : PermitResponse

data class PartiallyPermitted(val permitted: MutableMainModel) : PermitResponse

object NotPermitted : PermitResponse