package org.treeWare.metaModel

import org.treeWare.model.core.MainModel

data class ValidatedMetaModel(val metaModel: MainModel?, val errors: List<String>)