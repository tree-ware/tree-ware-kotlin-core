package org.treeWare.model.core

fun newChildValue(parent: MutableElementModel): MutableElementModel = when (parent.elementType) {
    ModelElementType.MAIN, ModelElementType.SINGLE_FIELD -> (parent as MutableSingleFieldModel).getOrNewValue()
    ModelElementType.LIST_FIELD -> (parent as MutableListFieldModel).getNewValue()
    ModelElementType.SET_FIELD -> (parent as MutableSetFieldModel).getNewValue()
    ModelElementType.ASSOCIATION -> (parent as MutableAssociationModel).value
    else -> throw IllegalStateException("Unsupported parent type: ${parent.elementType}")
}