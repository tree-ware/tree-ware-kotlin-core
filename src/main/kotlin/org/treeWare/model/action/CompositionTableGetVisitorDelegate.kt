package org.treeWare.model.action

import org.treeWare.model.core.BaseEntityModel
import org.treeWare.model.core.MutableCompositionListFieldModel
import org.treeWare.model.core.MutableRootModel
import org.treeWare.schema.core.EntitySchema

// IMPLEMENTATION: ./Get.md

interface CompositionTableGetVisitorDelegate<MappingAux> {
    fun pushPathEntity(entity: BaseEntityModel<*>, entitySchema: EntitySchema)
    fun popPathEntity()

    suspend fun fetchRoot(
        responseRoot: MutableRootModel<Unit>,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )

    suspend fun fetchCompositionList(
        responseListField: MutableCompositionListFieldModel<Unit>,
        requestFieldNames: List<String>,
        mappingAux: MappingAux
    )
}
