package org.treeWare.model.operator

import org.treeWare.model.core.MutableMainModel

data class DifferenceModels(
    val createModel: MutableMainModel?,
    val deleteModel: MutableMainModel?,
    val updateModel: MutableMainModel?
)