package org.treeWare.model.operator.get

import org.treeWare.model.core.MainModel

sealed class GetResponse {
    data class Model(val model: MainModel) : GetResponse()
    data class ErrorList(val errorList: List<String>) : GetResponse()

    /** A model with "error_" aux. */
    data class ErrorModel(val errorModel: MainModel) : GetResponse()
}