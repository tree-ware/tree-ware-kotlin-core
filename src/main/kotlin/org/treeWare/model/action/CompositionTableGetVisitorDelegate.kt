package org.treeWare.model.action

import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.MutableListFieldModel
import org.treeWare.model.core.MutableRootModel

// IMPLEMENTATION: ./Get.md

interface CompositionTableGetVisitorDelegate<MappingAux> {
    fun pushPathEntity(entity: BaseEntityModel<*>)
    fun popPathEntity()

    suspend fun fetchRoot(
        responseRoot: MutableRootModel<Unit>,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )

    suspend fun fetchCompositionList(
        responseListField: MutableListFieldModel<Unit>,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )
}
