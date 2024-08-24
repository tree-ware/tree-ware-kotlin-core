package org.treeWare.metaModel

import org.treeWare.model.core.EntityModel

data class ValidatedMetaModel(val metaModel: EntityModel?, val errors: List<String>)