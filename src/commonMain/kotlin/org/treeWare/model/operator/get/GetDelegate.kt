package org.treeWare.model.operator.get

import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.ErrorCode

sealed class GetCompositionResult(open val errorCode: ErrorCode) {
    data class Entity(val entity: MutableEntityModel) : GetCompositionResult(ErrorCode.OK)
    data class ErrorList(override val errorCode: ErrorCode, val errorList: List<ElementModelError>) :
        GetCompositionResult(errorCode)
}

sealed class GetCompositionSetResult(open val errorCode: ErrorCode) {
    data class Entities(val entities: List<MutableEntityModel>) : GetCompositionSetResult(ErrorCode.OK)
    data class ErrorList(override val errorCode: ErrorCode, val errorList: List<ElementModelError>) :
        GetCompositionSetResult(errorCode)
}

interface GetDelegate {
    fun getRoot(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseRootEntity: MutableEntityModel
    ): GetCompositionResult

    fun getComposition(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSingleFieldModel?
    ): GetCompositionResult

    fun getCompositionSet(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestKeys: List<SingleFieldModel>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSetFieldModel
    ): GetCompositionSetResult
}