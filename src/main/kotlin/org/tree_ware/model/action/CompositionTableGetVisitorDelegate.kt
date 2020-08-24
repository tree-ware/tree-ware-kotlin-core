package org.tree_ware.model.action

import org.tree_ware.model.core.BaseEntityModel
import org.tree_ware.model.core.MutableCompositionListFieldModel
import org.tree_ware.model.core.MutableRootModel
import org.tree_ware.schema.core.EntitySchema

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
