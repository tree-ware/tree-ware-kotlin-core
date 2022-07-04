package org.treeWare.model.operator.get

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.ErrorCode

sealed class GetResponse(open val errorCode: ErrorCode) {
    fun isOk(): Boolean = errorCode == ErrorCode.OK

    data class Model(val model: MainModel) : GetResponse(ErrorCode.OK)
    data class ErrorList(override val errorCode: ErrorCode, val errorList: List<ElementModelError>) :
        GetResponse(errorCode)

    /** A model with "error_" aux. */
    data class ErrorModel(override val errorCode: ErrorCode, val errorModel: MainModel) : GetResponse(errorCode)
}