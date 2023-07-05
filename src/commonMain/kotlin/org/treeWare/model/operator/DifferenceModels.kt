package org.treeWare.model.operator

import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.MutableMainModelFactory

data class DifferenceModels<O : MutableMainModel>(
    val createModel: O,
    val deleteModel: O,
    val updateModel: O
) {
    /**===== Returns true if the original and new were not identical =====**/
    fun isDifferent(): Boolean =
        !createModel.isEmpty() || !deleteModel.isEmpty() || !updateModel.isEmpty()
}

fun <O : MutableMainModel> newDifferenceModels(mutableMainModelFactory: MutableMainModelFactory<O>): DifferenceModels<O> {
    return DifferenceModels(
        mutableMainModelFactory.createInstance(),
        mutableMainModelFactory.createInstance(),
        mutableMainModelFactory.createInstance(),
    )
}
