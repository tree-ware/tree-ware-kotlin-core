package org.treeWare.model.core

fun <Aux> getNewFieldValue(field: MutableElementModel<Aux>): MutableElementModel<Aux> = when (field.elementType) {
    ModelElementType.SINGLE_FIELD -> (field as MutableSingleFieldModel<Aux>).getOrNewValue()
    ModelElementType.LIST_FIELD -> (field as MutableListFieldModel<Aux>).getNewValue()
    ModelElementType.SET_FIELD -> (field as MutableSetFieldModel<Aux>).getNewValue()
    else -> throw IllegalStateException("Not a field type: ${field.elementType}")
}
