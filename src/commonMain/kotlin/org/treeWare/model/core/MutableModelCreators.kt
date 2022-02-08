package org.treeWare.model.core

fun getNewFieldValue(field: MutableElementModel): MutableElementModel = when (field.elementType) {
    ModelElementType.MAIN, ModelElementType.SINGLE_FIELD -> (field as MutableSingleFieldModel).getOrNewValue()
    ModelElementType.LIST_FIELD -> (field as MutableListFieldModel).getNewValue()
    ModelElementType.SET_FIELD -> (field as MutableSetFieldModel).getNewValue()
    else -> throw IllegalStateException("Not a field type: ${field.elementType}")
}
