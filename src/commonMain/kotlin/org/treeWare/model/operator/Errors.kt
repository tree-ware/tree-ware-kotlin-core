package org.treeWare.model.operator

import org.treeWare.model.core.MainModel

sealed class Errors(open val errorCode: ErrorCode) {
    fun isOk(): Boolean = errorCode == ErrorCode.OK

    object None : Errors(ErrorCode.OK)

    data class ErrorList(override val errorCode: ErrorCode, val errorList: List<ElementModelError>) :
        Errors(errorCode)

    /** A model with "error_" aux. */
    data class ErrorModel(override val errorCode: ErrorCode, val errorModel: MainModel) : Errors(errorCode)
}
