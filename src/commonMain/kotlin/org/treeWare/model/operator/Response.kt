package org.treeWare.model.operator

import org.treeWare.model.core.EntityModel

sealed class Response(open val errorCode: ErrorCode) {
    fun isOk(): Boolean = errorCode == ErrorCode.OK

    object Success : Response(ErrorCode.OK)

    data class Model(val model: EntityModel) : Response(ErrorCode.OK)

    data class ErrorList(override val errorCode: ErrorCode, val errorList: List<ElementModelError>) :
        Response(errorCode)

    /** A model with "error_" aux. */
    data class ErrorModel(override val errorCode: ErrorCode, val errorModel: EntityModel) : Response(errorCode)
}