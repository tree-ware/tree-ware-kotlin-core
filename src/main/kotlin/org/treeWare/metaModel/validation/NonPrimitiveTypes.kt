package org.treeWare.metaModel.validation

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.Resolved

data class NonPrimitiveTypes(
    val enumerations: Map<String, EntityModel<Resolved>>,
    val entities: Map<String, EntityModel<Resolved>>
)
