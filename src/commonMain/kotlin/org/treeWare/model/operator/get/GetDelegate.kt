package org.treeWare.model.operator.get

import org.treeWare.model.core.*

interface GetDelegate {
    fun fetchComposition(
        parentEntityPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSingleFieldModel
    ): MutableEntityModel?

    fun fetchCompositionSet(
        parentEntityPath: String,
        ancestorKeys: List<Keys>,
        requestKeys: List<SingleFieldModel>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSetFieldModel
    ): List<MutableEntityModel>
}