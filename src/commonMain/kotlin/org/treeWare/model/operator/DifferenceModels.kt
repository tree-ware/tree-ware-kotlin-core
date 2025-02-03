package org.treeWare.model.operator

import org.treeWare.model.core.EntityFactory
import org.treeWare.model.core.MutableEntityModel

data class DifferenceModels(
    val createModel: MutableEntityModel,
    val deleteModel: MutableEntityModel,
    val updateModel: MutableEntityModel
) {
    fun isDifferent(): Boolean =
        !createModel.isEmpty() || !deleteModel.isEmpty() || !updateModel.isEmpty()
}

fun newDifferenceModels(entityFactory: EntityFactory): DifferenceModels {
    return DifferenceModels(
        entityFactory(null),
        entityFactory(null),
        entityFactory(null),
    )
}