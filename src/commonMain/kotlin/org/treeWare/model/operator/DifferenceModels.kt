package org.treeWare.model.operator

import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableEntityModelFactory

data class DifferenceModels(
    val createModel: MutableEntityModel,
    val deleteModel: MutableEntityModel,
    val updateModel: MutableEntityModel
) {
    /**===== Returns true if the original and new were not identical =====**/
    fun isDifferent(): Boolean =
        !createModel.isEmpty() || !deleteModel.isEmpty() || !updateModel.isEmpty()
}

fun newDifferenceModels(mutableEntityModelFactory: MutableEntityModelFactory): DifferenceModels {
    return DifferenceModels(
        mutableEntityModelFactory.create(),
        mutableEntityModelFactory.create(),
        mutableEntityModelFactory.create(),
    )
}