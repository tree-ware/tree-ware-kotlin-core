package org.treeWare.metaModel

import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.core.MutableMainModelFactory

private val metaMetaModel = newMainMetaMetaModel()

object MetaModelMutableMainModelFactory : MutableMainModelFactory<MutableMainModel> {
    override fun createInstance(): MutableMainModel {
        return MutableMainModel(metaMetaModel)
    }
}
