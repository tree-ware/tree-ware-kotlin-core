package org.treeWare.model.operator.set

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.ElementModelError

sealed class SetResponse {
    object Success : SetResponse()

    data class ErrorList(val errorList: List<ElementModelError>) : SetResponse()

    /** A model with "error_" aux. */
    data class ErrorModel(val errorModel: MainModel) : SetResponse()
}