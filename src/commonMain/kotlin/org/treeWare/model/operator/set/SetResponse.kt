package org.treeWare.model.operator.set

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.ErrorCode

sealed class SetResponse(open val errorCode: ErrorCode) {
    object Success : SetResponse(ErrorCode.OK)

    data class ErrorList(override val errorCode: ErrorCode, val errorList: List<ElementModelError>) :
        SetResponse(errorCode)

    /** A model with "error_" aux. */
    data class ErrorModel(override val errorCode: ErrorCode, val errorModel: MainModel) : SetResponse(errorCode)
}