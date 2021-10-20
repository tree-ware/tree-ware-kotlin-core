package org.treeWare.model.action

import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.MutableRootModel
import org.treeWare.model.core.MutableSetFieldModel

// IMPLEMENTATION: ./Get.md

interface CompositionTableGetVisitorDelegate<MappingAux> {
    fun pushPathEntity(entity: BaseEntityModel)
    fun popPathEntity()

    suspend fun fetchRoot(
        responseRoot: MutableRootModel,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )

    suspend fun fetchCompositionList(
        responseListField: MutableSetFieldModel,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )
}
