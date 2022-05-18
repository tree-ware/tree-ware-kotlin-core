package org.treeWare.model.operator.get

import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError

sealed class FetchCompositionResult {
    data class Entity(val entity: MutableEntityModel) : FetchCompositionResult()
    data class ErrorList(val errorList: List<ElementModelError>) : FetchCompositionResult()
}

sealed class FetchCompositionSetResult {
    data class Entities(val entities: List<MutableEntityModel>) : FetchCompositionSetResult()
    data class ErrorList(val errorList: List<ElementModelError>) : FetchCompositionSetResult()
}

interface GetDelegate {
    fun fetchComposition(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSingleFieldModel
    ): FetchCompositionResult

    fun fetchCompositionSet(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestKeys: List<SingleFieldModel>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSetFieldModel
    ): FetchCompositionSetResult
}