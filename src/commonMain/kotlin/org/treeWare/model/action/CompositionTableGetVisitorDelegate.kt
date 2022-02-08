package org.treeWare.model.action

import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableSetFieldModel

// IMPLEMENTATION: ./Get.md

interface CompositionTableGetVisitorDelegate<MappingAux> {
    fun pushPathEntity(entity: BaseEntityModel)
    fun popPathEntity()

    // TODO(deepak-nulu): is this method needed given that root entites are regular entities now?
    fun fetchRoot(
        responseRoot: MutableEntityModel,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )

    fun fetchCompositionList(
        responseListField: MutableSetFieldModel,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )
}