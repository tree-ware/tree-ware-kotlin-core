package org.treeWare.model.operator

import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.MutableMainModelFactory

data class DifferenceModels(
    val createModel: MutableMainModel,
    val deleteModel: MutableMainModel,
    val updateModel: MutableMainModel
) {
    /**===== Returns true if the original and new were not identical =====**/
    fun isDifferent(): Boolean =
        !createModel.isEmpty() || !deleteModel.isEmpty() || !updateModel.isEmpty()
}

fun newDifferenceModels(mutableMainModelFactory: MutableMainModelFactory): DifferenceModels {
    return DifferenceModels(
        mutableMainModelFactory.createInstance(),
        mutableMainModelFactory.createInstance(),
        mutableMainModelFactory.createInstance(),
    )
}
