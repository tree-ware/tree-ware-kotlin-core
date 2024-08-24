package org.treeWare.metaModel

import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableEntityModelFactory

private val metaMetaModel = newMetaMetaModel()
private val metaRootEntityMeta = getModelRootEntityMeta(metaMetaModel)

object MetaModelMutableEntityModelFactory : MutableEntityModelFactory {
    override fun create(): MutableEntityModel {
        return MutableEntityModel(metaRootEntityMeta, null)
    }
}