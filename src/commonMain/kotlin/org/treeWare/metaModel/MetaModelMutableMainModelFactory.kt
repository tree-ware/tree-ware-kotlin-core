package org.treeWare.metaModel

import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.MutableMainModelFactory

private val metaMetaModel = newMainMetaMetaModel()

object MetaModelMutableMainModelFactory : MutableMainModelFactory {
    override fun getNewInstance(): MutableMainModel {
        return MutableMainModel(metaMetaModel)
    }
}
