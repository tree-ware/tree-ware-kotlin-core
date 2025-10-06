package org.treeWare.metaModel

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableFieldModel

/** Provides access to meta-model related information. It will be implemented in code generated for a meta-model. */
interface MetaModelInfo {
    val metaModelFiles: List<String>
    val metaModel: EntityModel
    fun rootEntityFactory(parent: MutableFieldModel?): MutableEntityModel
}