package org.treeWare.metaModel.validation

import org.treeWare.model.core.EntityModel

data class NonPrimitiveTypes(
    val enumerations: Map<String, EntityModel>,
    val entities: Map<String, EntityModel>
)
