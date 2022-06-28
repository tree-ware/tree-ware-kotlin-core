package org.treeWare.model.operator

import org.treeWare.model.core.MutableMainModel

data class DifferenceModels(
    val createModel: MutableMainModel?,
    val deleteModel: MutableMainModel?,
    val updateModel: MutableMainModel?
){
    /**===== Returns true if the original and new were not identical =====**/
    fun isDifferent(): Boolean =
        createModel != null || deleteModel != null || updateModel !=null
}