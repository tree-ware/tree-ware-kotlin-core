package org.treeWare.metaModel

import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableFieldModel

private val metaMetaModel = newMetaMetaModel()

private val metaModelRootEntityMeta = getResolvedRootMeta(metaMetaModel)

fun metaModelRootEntityFactory(parent: MutableFieldModel?) =
    MutableEntityModel(metaModelRootEntityMeta, parent)
