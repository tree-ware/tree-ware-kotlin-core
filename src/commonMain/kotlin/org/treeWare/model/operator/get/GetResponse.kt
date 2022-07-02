package org.treeWare.model.operator.get

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.ElementModelError

sealed class GetResponse {
    data class Model(val model: MainModel) : GetResponse()
    data class ErrorList(val errorList: List<ElementModelError>) : GetResponse()

    /** A model with "error_" aux. */
    data class ErrorModel(val errorModel: MainModel) : GetResponse()
}