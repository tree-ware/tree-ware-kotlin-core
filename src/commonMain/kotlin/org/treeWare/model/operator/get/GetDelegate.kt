package org.treeWare.model.operator.get

import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError

sealed class GetCompositionResult {
    data class Entity(val entity: MutableEntityModel) : GetCompositionResult()
    data class ErrorList(val errorList: List<ElementModelError>) : GetCompositionResult()
}

sealed class GetCompositionSetResult {
    data class Entities(val entities: List<MutableEntityModel>) : GetCompositionSetResult()
    data class ErrorList(val errorList: List<ElementModelError>) : GetCompositionSetResult()
}

interface GetDelegate {
    fun getComposition(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSingleFieldModel
    ): GetCompositionResult

    fun getCompositionSet(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestKeys: List<SingleFieldModel>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSetFieldModel
    ): GetCompositionSetResult
}