package org.treeWare.model.core

import org.treeWare.metaModel.getFieldTypeMeta

fun <Aux> newMutableValueModel(
    fieldMeta: EntityModel<Resolved>?,
    parent: MutableFieldModel<Aux>
): MutableElementModel<Aux> {
    if (fieldMeta == null) throw IllegalStateException("fieldMeta is null when creating mutable value model")
    return when (getFieldTypeMeta(fieldMeta)) {
        "enumeration" -> MutableEnumerationModel(parent)
        "association" -> MutableAssociationModel(parent)
        "entity" -> MutableEntityModel(fieldMeta.aux?.entityMeta, parent)
        else -> MutablePrimitiveModel(parent)
    }
}
