package org.treeWare.model.core

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta

fun <Aux> newMutableValueModel(
    fieldMeta: EntityModel<Resolved>?,
    parent: MutableFieldModel<Aux>
): MutableElementModel<Aux> {
    if (fieldMeta == null) throw IllegalStateException("fieldMeta is null when creating mutable value model")
    return when (getFieldTypeMeta(fieldMeta)) {
        FieldType.ENUMERATION -> MutableEnumerationModel(parent)
        FieldType.ASSOCIATION -> MutableAssociationModel(parent)
        FieldType.COMPOSITION -> MutableEntityModel(fieldMeta.aux?.compositionMeta, parent)
        else -> MutablePrimitiveModel(parent)
    }
}
