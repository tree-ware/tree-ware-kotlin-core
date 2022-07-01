package org.treeWare.model.operator.set

import org.treeWare.model.core.MainModel

sealed class SetResponse {
    data class ErrorList(val errorList: List<String>) : SetResponse()

    /** A model with "error_" aux. */
    data class ErrorModel(val errorModel: MainModel) : SetResponse()
}