package org.treeWare.model.core

import org.treeWare.metaModel.Multiplicity
import org.treeWare.metaModel.getFieldMeta
import org.treeWare.metaModel.getMultiplicityMeta


/**===== Create a new child value without attaching from the parent end =====**/
fun newDisconnectedValue(parent: MutableElementModel): MutableElementModel = when (parent.elementType) {
    ModelElementType.MAIN, ModelElementType.SINGLE_FIELD -> (parent as MutableSingleFieldModel).value
        ?: (if (isKeyField(parent)) parent.getNewValue() else newMutableValueModel(parent.meta, parent))
    ModelElementType.LIST_FIELD -> newMutableValueModel((parent as MutableListFieldModel).meta, parent)
    ModelElementType.SET_FIELD -> newMutableValueModel((parent as MutableSetFieldModel).meta, parent)
    //There is no need to make children of associations disconnected
    ModelElementType.ASSOCIATION -> (parent as MutableAssociationModel).value//Automatically connects

    else -> throw IllegalStateException("Unsupported parent type: ${parent.elementType}")
}


/**===== Attach a previously made child value from the parent end =====**/
fun connectValue(parent: MutableElementModel, child: MutableElementModel) = when (parent.elementType) {
    ModelElementType.MAIN, ModelElementType.SINGLE_FIELD -> (parent as MutableSingleFieldModel).setValue(child)
    ModelElementType.LIST_FIELD -> (parent as MutableListFieldModel).addValue(child)
    ModelElementType.SET_FIELD -> (parent as MutableSetFieldModel).addValue(child)
    ModelElementType.ASSOCIATION -> {}//Nothing to be done

    else -> throw IllegalStateException("Unsupported parent type: ${parent.elementType}")
}

/**===== Make a new field without attaching it on the parent end =====**/
fun newDisconnectedField(parent: MutableBaseEntityModel, fieldName: String): MutableFieldModel {
    val existing = parent.getField(fieldName)
    if (existing != null) return existing
    val fieldMeta = parent.meta?.let { getFieldMeta(it, fieldName) }
        ?: throw IllegalStateException("fieldMeta is null when creating mutable field model")
    val newField = when (getMultiplicityMeta(fieldMeta)) {
        Multiplicity.LIST -> MutableListFieldModel(fieldMeta, parent)
        Multiplicity.SET -> MutableSetFieldModel(fieldMeta, parent)
        else -> MutableSingleFieldModel(fieldMeta, parent)
    }
    return newField
}

/**===== Add a previously created child field to the parent's list of fields =====**/
fun connectField(parent: MutableBaseEntityModel, child: MutableFieldModel) {
    parent.fields[getFieldName(child)] = child
}